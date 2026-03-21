package backend.academy.linktracker.scrapper.client.interfaces;

import backend.academy.linktracker.scrapper.dto.LinkUpdate;

public interface BotClient {
    void sendUpdate(LinkUpdate update);
}
