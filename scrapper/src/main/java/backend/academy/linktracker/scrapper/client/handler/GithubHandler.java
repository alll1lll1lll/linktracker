package backend.academy.linktracker.scrapper.client.handler;

import backend.academy.linktracker.scrapper.client.github.GitHubClient;
import backend.academy.linktracker.scrapper.client.interfaces.BotClient;
import backend.academy.linktracker.scrapper.model.LinkModel;
import backend.academy.linktracker.scrapper.parser.GitHubParser;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GithubHandler extends AbstractLinkHandler {
    private final GitHubClient client;
    private final GitHubParser parser;

    public GithubHandler(
            BotClient botClient,
            GitHubClient client,
            GitHubParser parser,
            SubscriptionRepository subscriptionRepository,
            LinkRepository linkRepository) {
        super(botClient, subscriptionRepository, linkRepository);
        this.client = client;
        this.parser = parser;
    }

    @Override
    public String getHost() {
        return "github.com";
    }

    @Override
    protected Optional<OffsetDateTime> fetchUpdate(LinkModel link) {
        return parser.parseRepositoryInfo(link.getUrl())
                .map(info -> client.fetchRepoInfo(info.owner(), info.repo()))
                .map(r -> r.getPushedAt() != null ? r.getPushedAt() : r.getUpdatedAt());
    }

    @Override
    protected String getDescription(LinkModel link) {
        return "в репозитории произошло обновление.";
    }

    @Override
    protected void logUpdate(LinkModel link, OffsetDateTime oldDate, OffsetDateTime newDate) {
        log.atInfo()
                .addKeyValue("event", "github_update")
                .addKeyValue("url", link.getUrl())
                .addKeyValue("old_date", oldDate)
                .addKeyValue("new_date", newDate)
                .log("GitHub update processed");
    }
}
