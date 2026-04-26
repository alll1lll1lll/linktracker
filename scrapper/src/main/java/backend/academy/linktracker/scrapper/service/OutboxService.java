package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.client.interfaces.BotClient;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.entity.OutboxEntity;
import backend.academy.linktracker.scrapper.repository.OutboxRepository;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxRepository outboxRepository;

    @Qualifier("transportBotClient")
    private final BotClient transportClient;

    @Transactional
    public void queueUpdate(LinkUpdate update) {
        OutboxEntity entity = OutboxEntity.builder()
                .linkId(update.getId())
                .url(update.getUrl().toString())
                .description(update.getDescription())
                .chatIds(update.getTgChatIds())
                .build();

        outboxRepository.save(entity);
    }

    @Transactional
    public void processPendingEvents(int batchSize) {
        List<OutboxEntity> events = outboxRepository.findPendingEvents(batchSize);
        if (events.isEmpty()) return;

        for (OutboxEntity event : events) {
            try {
                transportClient.sendUpdate(mapToDto(event));
                outboxRepository.delete(event.getId());
            } catch (Exception e) {
                log.error("Failed to dispatch outbox event {}: {}", event.getId(), e.getMessage());
                break;
            }
        }
    }

    private LinkUpdate mapToDto(OutboxEntity entity) {
        return new LinkUpdate(
                entity.getLinkId(), URI.create(entity.getUrl()), entity.getDescription(), entity.getChatIds());
    }
}
