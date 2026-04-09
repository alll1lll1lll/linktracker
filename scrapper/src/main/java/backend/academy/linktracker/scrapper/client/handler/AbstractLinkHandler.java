package backend.academy.linktracker.scrapper.client.handler;

import backend.academy.linktracker.scrapper.client.interfaces.BotClient;
import backend.academy.linktracker.scrapper.client.interfaces.LinkHandler;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.model.LinkModel;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractLinkHandler implements LinkHandler {

    private final BotClient botClient;
    private final SubscriptionRepository subscriptionRepository;
    private final LinkRepository linkRepository;

    @Override
    @Transactional
    public void handle(LinkModel link) {
        fetchUpdate(link).ifPresent(newLastUpdate -> {
            OffsetDateTime currentLastUpdate = link.getLastUpdated();
            if (currentLastUpdate == null) {
                log.atInfo()
                        .addKeyValue("event", "link_baseline_initialized")
                        .addKeyValue("url", link.getUrl())
                        .log("set initial update time for link");
                link.setLastUpdated(newLastUpdate);
                linkRepository.updateLastUpdated(link.getId(), newLastUpdate);
            } else if (newLastUpdate.isAfter(currentLastUpdate)) {
                link.setLastUpdated(newLastUpdate);
                linkRepository.updateLastUpdated(link.getId(), newLastUpdate);
                logUpdate(link, currentLastUpdate, newLastUpdate);
                sendUpdate(link, getDescription(link));
            }
        });
    }

    protected abstract Optional<OffsetDateTime> fetchUpdate(LinkModel link);

    protected abstract String getDescription(LinkModel link);

    protected abstract void logUpdate(LinkModel link, OffsetDateTime old, OffsetDateTime current);

    private void sendUpdate(LinkModel link, String description) {
        List<Long> chatIds = subscriptionRepository.findChatSubscribers(link.getId());
        if (chatIds.isEmpty()) {
            return;
        }
        try {
            botClient.sendUpdate(new LinkUpdate(link.getId(), link.getUrl(), description, chatIds));
        } catch (Exception e) {
            log.atError()
                    .addKeyValue("event", "bot_notification_failed")
                    .log("Failed to send gRPC update to bot, but continuing transaction", e);
        }
    }
}
