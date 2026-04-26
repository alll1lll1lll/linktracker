package backend.academy.linktracker.scrapper.scheduler;

import backend.academy.linktracker.scrapper.service.OutboxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OutboxScheduler {

    private final OutboxService outboxService;
    private final int batchSize;

    public OutboxScheduler(OutboxService outboxService, @Value("${app.outbox.scheduler.batch-size}") int batchSize) {
        this.outboxService = outboxService;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${app.outbox.scheduler.delay-ms}")
    public void processOutbox() {
        log.atDebug().log("Starting outbox processing batch with size: {}", batchSize);
        outboxService.processPendingEvents(batchSize);
    }
}
