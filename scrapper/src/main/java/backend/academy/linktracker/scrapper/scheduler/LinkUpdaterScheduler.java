package backend.academy.linktracker.scrapper.scheduler;

import static org.slf4j.MDC.putCloseable;

import backend.academy.linktracker.scrapper.model.LinkModel;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.service.LinkUpdateService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkUpdaterScheduler {
    private final LinkRepository linkRepository;
    private final LinkUpdateService linkUpdateService;

    @Scheduled(fixedDelayString = "${app.scheduler.link-update-delay}")
    public void update() {
        try (var _ = putCloseable("run_id", UUID.randomUUID().toString())) {
            log.atInfo().addKeyValue("event", "scheduler_start").log("start updating links");
            List<LinkModel> links = linkRepository.findAll();
            if (links.isEmpty()) {
                log.atInfo().addKeyValue("event", "no_links_to_update").log("tracked links not found.");
                return;
            }
            links.forEach(linkUpdateService::processUpdate);
            log.atInfo()
                    .addKeyValue("event", "scheduler_finish")
                    .addKeyValue("processed_count", links.size())
                    .log("task was done");
        }
    }
}
