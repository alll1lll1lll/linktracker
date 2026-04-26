package backend.academy.linktracker.bot.service.notification;

import backend.academy.linktracker.avro.LinkUpdateEvent;
import backend.academy.linktracker.bot.dto.LinkUpdate;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.client-type", havingValue = "kafka")
public class LinkUpdateListener {

    private final NotificationService notificationService;
    private static final String NO_DESCRIPTION = "No description";
    private final Set<Long> processedMessageIds = ConcurrentHashMap.newKeySet();

    @KafkaListener(topics = "${app.kafka.topic-name:link-updates}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(ConsumerRecord<String, LinkUpdateEvent> record) {
        log.atInfo().log("message received from Kafka. Offset: {}", record.offset());

        try {
            processEvent(record.value());
        } catch (IllegalArgumentException e) {
            log.atError().setCause(e).log("validation failed. Sending to DLQ.");
            throw e;
        } catch (Exception e) {
            log.atError().setCause(e).log("failed to process Kafka message. Will retry.");
            removeFromCacheOnError(record.value());
            throw e;
        }
    }

    private void processEvent(LinkUpdateEvent event) {
        if (event == null) {
            log.atWarn().log("Received null event, skipping");
            return;
        }

        validateEvent(event);

        if (isDuplicate(event.getId())) {
            return;
        }

        LinkUpdate dto = mapToDto(event);

        log.atInfo().log("processing update for URL: {} (Chats: {})", dto.getUrl(), dto.getTgChatIds());
        notificationService.processUpdate(dto);
        log.atInfo().log("successfully sent update to NotificationService");
    }

    private void validateEvent(LinkUpdateEvent event) {
        if (event.getUrl() == null || String.valueOf(event.getUrl()).isBlank()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
    }

    private boolean isDuplicate(Long eventId) {
        if (!processedMessageIds.add(eventId)) {
            log.atWarn().log("duplicate message ignored. Event ID: {}", eventId);
            return true;
        }
        return false;
    }

    private LinkUpdate mapToDto(LinkUpdateEvent event) {
        String urlStr = String.valueOf(event.getUrl());
        String description = event.getDescription() != null ? String.valueOf(event.getDescription()) : NO_DESCRIPTION;

        return LinkUpdate.builder()
                .id(event.getId())
                .url(URI.create(urlStr))
                .description(description)
                .tgChatIds(extractChatIds(event.getTgChatIds()))
                .build();
    }

    private List<Long> extractChatIds(List<?> rawChatIds) {
        if (rawChatIds == null || rawChatIds.isEmpty()) {
            return Collections.emptyList();
        }
        return rawChatIds.stream().map(id -> Long.parseLong(id.toString())).toList();
    }

    private void removeFromCacheOnError(LinkUpdateEvent event) {
        if (event != null) {
            processedMessageIds.remove(event.getId());
        }
    }
}
