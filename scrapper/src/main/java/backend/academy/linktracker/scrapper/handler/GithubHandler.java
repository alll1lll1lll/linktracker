package backend.academy.linktracker.scrapper.handler;

import backend.academy.linktracker.scrapper.client.github.GitHubClient;
import backend.academy.linktracker.scrapper.client.github.GithubEventType;
import backend.academy.linktracker.scrapper.client.interfaces.BotClient;
import backend.academy.linktracker.scrapper.dto.response.UpdateDetails;
import backend.academy.linktracker.scrapper.dto.response.github.GithubEventResponse;
import backend.academy.linktracker.scrapper.format.MessageFormatter;
import backend.academy.linktracker.scrapper.model.LinkModel;
import backend.academy.linktracker.scrapper.parser.GitHubParser;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GithubHandler extends AbstractLinkHandler {
    private final GitHubClient client;
    private final GitHubParser parser;
    private static final String ACTION_OPENED = "opened";
    private static final String UNKNOWN = "Unknown";

    public GithubHandler(
            BotClient botClient,
            GitHubClient client,
            GitHubParser parser,
            SubscriptionRepository subscriptionRepository,
            LinkRepository linkRepository,
            MessageFormatter messageFormatter) {
        super(botClient, subscriptionRepository, linkRepository, messageFormatter);
        this.client = client;
        this.parser = parser;
    }

    @Override
    public String getHost() {
        return "github.com";
    }

    @Override
    protected List<UpdateDetails> fetchUpdatesSince(LinkModel link, OffsetDateTime since) {
        return parser
                .parseRepositoryInfo(link.getUrl())
                .map(info -> client.fetchEvents(info.owner(), info.repo()))
                .stream()
                .flatMap(List::stream)
                .filter(event ->
                        event.getCreatedAt() != null && event.getCreatedAt().isAfter(since))
                .filter(this::isRelevantEvent)
                .map(this::mapToDetails)
                .toList();
    }

    private boolean isRelevantEvent(GithubEventResponse event) {
        GithubEventType type = GithubEventType.fromApiType(event.getType());
        if (type == null) {
            return false;
        }
        var payload = event.getPayload();
        return payload != null && ACTION_OPENED.equals(payload.getAction());
    }

    private UpdateDetails mapToDetails(GithubEventResponse event) {
        GithubEventType type = GithubEventType.fromApiType(event.getType());
        var payload = event.getPayload();
        String title = "";
        String body = "";

        if (type != null) {
            switch (type) {
                case GithubEventType.ISSUE -> {
                    if (payload.getIssue() != null) {
                        title = payload.getIssue().getTitle();
                        body = payload.getIssue().getBody();
                    }
                }
                case GithubEventType.PULL_REQUEST -> {
                    if (payload.getPullRequest() != null) {
                        title = payload.getPullRequest().getTitle();
                        body = payload.getPullRequest().getBody();
                    }
                }
            }
        }

        String author = event.getActor() != null && event.getActor().getLogin() != null
                ? event.getActor().getLogin()
                : UNKNOWN;

        return UpdateDetails.builder()
                .updateType(type.getApiType())
                .author(author)
                .createdAt(event.getCreatedAt())
                .title(title)
                .preview(body)
                .build();
    }
}
