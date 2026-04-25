package backend.academy.linktracker.scrapper.scheduler;

import backend.academy.linktracker.scrapper.model.LinkModel;
import backend.academy.linktracker.scrapper.properties.SchedulerProperties;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.service.LinkErrorReportService;
import backend.academy.linktracker.scrapper.service.LinkUpdateService;
import jakarta.annotation.PreDestroy;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LinkUpdaterScheduler {

    private final LinkRepository linkRepository;
    private final LinkUpdateService linkUpdateService;
    private final LinkErrorReportService errorReportService;
    private final SchedulerProperties properties;
    private final ExecutorService executorService;

    public LinkUpdaterScheduler(
            LinkRepository linkRepository,
            LinkUpdateService linkUpdateService,
            LinkErrorReportService errorReportService,
            SchedulerProperties properties) {
        this.linkRepository = linkRepository;
        this.linkUpdateService = linkUpdateService;
        this.errorReportService = errorReportService;
        this.properties = properties;
        this.executorService = Executors.newFixedThreadPool(properties.threads());
    }

    @Scheduled(fixedDelayString = "${app.scheduler.link-update-delay}")
    public void update() {
        try (var _ = MDC.putCloseable("run_id", UUID.randomUUID().toString())) {
            log.atInfo().addKeyValue("event", "scheduler_start").log("start updating links");

            OffsetDateTime now = OffsetDateTime.now();

            int totalProcessed = 0;
            while (true) {
                List<LinkModel> links = linkRepository.findLinksToUpdate(properties.batchSize(), now);

                if (links.isEmpty()) {
                    log.atDebug().log("no more links to update in this iteration");
                    break;
                }
                List<List<LinkModel>> chunks = partitionLinks(links);
                logProcessingInfo(links.size(), chunks.size());

                List<CompletableFuture<List<LinkModel>>> futures = submitTasks(chunks);
                List<LinkModel> allFailedLinks = waitForResultsAndCollectErrors(futures);

                if (!allFailedLinks.isEmpty()) {
                    errorReportService.sendErrorReport(allFailedLinks);
                }

                totalProcessed += links.size();

                if (links.size() < properties.batchSize()) {
                    break;
                }
            }
            log.atInfo()
                    .addKeyValue("total_processed", totalProcessed)
                    .log("successfully finished scheduler iteration");
        }
    }

    private List<List<LinkModel>> partitionLinks(List<LinkModel> links) {
        int threadCount = properties.threads();
        int totalSize = links.size();
        int chunkSize = Math.max(1, (totalSize + threadCount - 1) / threadCount);

        List<List<LinkModel>> chunks = new ArrayList<>();
        for (int i = 0; i < totalSize; i += chunkSize) {
            chunks.add(links.subList(i, Math.min(totalSize, i + chunkSize)));
        }
        return chunks;
    }

    private List<CompletableFuture<List<LinkModel>>> submitTasks(List<List<LinkModel>> chunks) {
        List<CompletableFuture<List<LinkModel>>> futures = new ArrayList<>();
        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        for (List<LinkModel> chunk : chunks) {
            futures.add(CompletableFuture.supplyAsync(
                    () -> {
                        if (contextMap != null) {
                            MDC.setContextMap(contextMap);
                        }
                        try {
                            return processChunk(chunk);
                        } finally {
                            MDC.clear();
                        }
                    },
                    executorService));
        }
        return futures;
    }

    private List<LinkModel> waitForResultsAndCollectErrors(List<CompletableFuture<List<LinkModel>>> futures) {
        List<LinkModel> failedLinks = new ArrayList<>();
        for (var future : futures) {
            try {
                failedLinks.addAll(future.join());
            } catch (Exception e) {
                log.atError().setCause(e).log("critical error while waiting for chunk result");
            }
        }
        return failedLinks;
    }

    private List<LinkModel> processChunk(List<LinkModel> chunk) {
        List<LinkModel> failedInChunk = new ArrayList<>();
        for (LinkModel link : chunk) {
            try {
                if (!linkUpdateService.processUpdate(link)) {
                    failedInChunk.add(link);
                }
            } catch (Exception e) {
                log.atError()
                        .setCause(e)
                        .addKeyValue("link_id", link.getId())
                        .addKeyValue("url", link.getUrl())
                        .log("unexpected error in chunk processing for link");
                failedInChunk.add(link);
            }
        }
        return failedInChunk;
    }

    private void logProcessingInfo(int linkCount, int chunkCount) {
        log.atInfo()
                .addKeyValue("link_count", linkCount)
                .addKeyValue("chunk_count", chunkCount)
                .addKeyValue("threads", properties.threads())
                .log("processing fetched links in chunks");
    }

    @PreDestroy
    public void shutdown() {
        log.atInfo().log("shutting down LinkUpdaterScheduler ExecutorService");
        executorService.shutdown();
    }
}
