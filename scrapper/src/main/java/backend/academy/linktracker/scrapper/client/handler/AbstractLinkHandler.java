package backend.academy.linktracker.scrapper.client.handler;

import backend.academy.linktracker.scrapper.client.interfaces.BotClient;
import backend.academy.linktracker.scrapper.client.interfaces.LinkHandler;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.model.LinkModel;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractLinkHandler implements LinkHandler {
    private final BotClient botClient;

    @Override
    public void handle(LinkModel link) {
        fetchUpdate(link).ifPresent(newLastUpdate -> {
            OffsetDateTime currentLastUpdate = link.getLastUpdated();

            if (currentLastUpdate == null) {
                link.setLastUpdated(newLastUpdate);
                return;
            }

            if (newLastUpdate.isAfter(currentLastUpdate)) {
                link.setLastUpdated(newLastUpdate);
                logUpdate(link, currentLastUpdate, newLastUpdate);
                sendUpdate(link, getDescription(link));
            }
        });
    }

    protected abstract Optional<OffsetDateTime> fetchUpdate(LinkModel link);

    protected abstract String getDescription(LinkModel link);

    protected abstract void logUpdate(LinkModel link, OffsetDateTime old, OffsetDateTime current);

    private void sendUpdate(LinkModel link, String description) {
        if (link.getChatSubscribers() == null || link.getChatSubscribers().isEmpty()) {
            return;
        }
        List<Long> chatIds = new ArrayList<>(link.getChatSubscribers().keySet());
        botClient.sendUpdate(new LinkUpdate(link.getId(), link.getUrl(), description, chatIds));
    }
}
