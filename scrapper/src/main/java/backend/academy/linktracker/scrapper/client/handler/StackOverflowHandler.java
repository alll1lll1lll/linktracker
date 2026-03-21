package backend.academy.linktracker.scrapper.client.handler;

import backend.academy.linktracker.scrapper.client.interfaces.BotClient;
import backend.academy.linktracker.scrapper.client.stackOverflow.StackOverflowClient;
import backend.academy.linktracker.scrapper.model.LinkModel;
import backend.academy.linktracker.scrapper.parser.StackOverflowParser;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StackOverflowHandler extends AbstractLinkHandler {
    private final StackOverflowClient client;
    private final StackOverflowParser parser;

    public StackOverflowHandler(BotClient botClient, StackOverflowClient client, StackOverflowParser parser) {
        super(botClient);
        this.client = client;
        this.parser = parser;
    }

    @Override
    public String getHost() {
        return "stackoverflow.com";
    }

    @Override
    protected Optional<OffsetDateTime> fetchUpdate(LinkModel link) {
        return parser.parseQuestionId(link.getUrl())
                .map(client::fetchQuestion)
                .filter(r -> r.getItems() != null && !r.getItems().isEmpty())
                .map(r -> r.getItems().getFirst().getLastActivityDate());
    }

    @Override
    protected String getDescription(LinkModel link) {
        return "появилась новая активность в вопросе на StackOverflow.";
    }

    @Override
    protected void logUpdate(LinkModel link, OffsetDateTime old, OffsetDateTime current) {
        log.info("SO Update: {} -> {}", old, current);
    }
}
