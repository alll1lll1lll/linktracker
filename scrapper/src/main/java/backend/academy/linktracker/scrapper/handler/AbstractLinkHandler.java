package backend.academy.linktracker.scrapper.handler;

import backend.academy.linktracker.scrapper.client.interfaces.BotClient;
import backend.academy.linktracker.scrapper.client.interfaces.LinkHandler;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.dto.response.UpdateDetails;
import backend.academy.linktracker.scrapper.format.MessageFormatter;
import backend.academy.linktracker.scrapper.model.LinkModel;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractLinkHandler implements LinkHandler {

    private final BotClient botClient;
    private final SubscriptionRepository subscriptionRepository;
    private final LinkRepository linkRepository;
    private final MessageFormatter messageFormatter;

    @Override
    public void handle(LinkModel link) {
        if (link.getLastUpdated() == null) {
            initializeBaseline(link);
            return;
        }

        List<UpdateDetails> updates = fetchUpdatesSince(link, link.getLastUpdated());

        if (updates == null || updates.isEmpty()) {
            return;
        }

        OffsetDateTime newestDate = updates.stream()
                .map(UpdateDetails::getCreatedAt)
                .max(OffsetDateTime::compareTo)
                .orElse(link.getLastUpdated());

        List<Long> chatIds = subscriptionRepository.findChatSubscribers(link.getId());
        if (chatIds.isEmpty()) {
            linkRepository.updateLastUpdated(link.getId(), newestDate);
            return;
        }

        for (UpdateDetails update : updates) {
            String description = messageFormatter.format(update);
            sendNotification(link, description, chatIds);
        }
        linkRepository.updateLastUpdated(link.getId(), newestDate);

        log.atInfo()
                .addKeyValue("url", link.getUrl())
                .addKeyValue("update_count", updates.size())
                .log("successfully processed updates and moved lastUpdated forward");
    }

    protected abstract List<UpdateDetails> fetchUpdatesSince(LinkModel link, OffsetDateTime since);

    private void initializeBaseline(LinkModel link) {
        OffsetDateTime now = OffsetDateTime.now();
        linkRepository.updateLastUpdated(link.getId(), now);
        log.atInfo().addKeyValue("url", link.getUrl()).log("initialized baseline for new link");
    }

    private void sendNotification(LinkModel link, String description, List<Long> chatIds) {
        try {
            botClient.sendUpdate(new LinkUpdate(link.getId(), link.getUrl(), description, chatIds));
        } catch (Exception e) {
            log.atError().setCause(e).addKeyValue("url", link.getUrl()).log("failed to notify bot");
        }
    }
}
