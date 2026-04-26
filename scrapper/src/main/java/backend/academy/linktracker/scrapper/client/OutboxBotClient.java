package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.client.interfaces.BotClient;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class OutboxBotClient implements BotClient {

    private final OutboxService outboxService;

    @Override
    public void sendUpdate(LinkUpdate update) {
        outboxService.queueUpdate(update);
    }
}
