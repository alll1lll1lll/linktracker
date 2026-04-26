package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.client.interfaces.BotClient;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component("transportBotClient")
@ConditionalOnProperty(name = "app.client-type", havingValue = "rest")
public class RestBotClient implements BotClient {
    private final RestClient restClient;

    public RestBotClient(@Qualifier("botRestClient") RestClient restClient) {
        this.restClient = restClient;
        log.atInfo().log("BotClient is active. Using: REST");
    }

    public void sendUpdate(LinkUpdate update) {
        restClient.post().uri("/updates").body(update).retrieve().toBodilessEntity();
    }
}
