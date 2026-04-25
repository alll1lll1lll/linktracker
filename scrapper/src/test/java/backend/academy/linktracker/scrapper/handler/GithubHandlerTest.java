package backend.academy.linktracker.scrapper.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.scrapper.client.github.GitHubClient;
import backend.academy.linktracker.scrapper.client.interfaces.BotClient;
import backend.academy.linktracker.scrapper.dto.response.github.*;
import backend.academy.linktracker.scrapper.format.MessageFormatter;
import backend.academy.linktracker.scrapper.model.LinkModel;
import backend.academy.linktracker.scrapper.parser.GitHubParser;
import backend.academy.linktracker.scrapper.parser.RepositoryInfo;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GithubHandlerTest {

    @Mock
    private GitHubClient gitHubClient;

    @Mock
    private BotClient botClient;

    @Mock
    private GitHubParser parser;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private LinkRepository linkRepository;

    private GithubHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GithubHandler(
                botClient, gitHubClient, parser, subscriptionRepository, linkRepository, new MessageFormatter());
    }

    @Test
    void shouldSendUpdateForNewIssue() {
        LinkModel link = new LinkModel(
                1L,
                URI.create("https://github.com/user/repo"),
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().minusDays(1));

        GithubEventResponse event = new GithubEventResponse();
        event.setType("IssuesEvent");
        event.setCreatedAt(OffsetDateTime.now());
        event.setActor(new GithubEventResponse.GithubActorResponse("cheremsha"));
        event.setPayload(new GithubEventResponse.GithubPayloadResponse(
                "opened",
                new GithubEventResponse.GithubIssueResponse(
                        "v sovrtskom soyuze...",
                        "вместо детектера лжи использовали черемшу, детекторов нет, а черемша в подвале жил, рычал на всех."),
                null));

        when(parser.parseRepositoryInfo(any())).thenReturn(Optional.of(new RepositoryInfo("user", "repo")));
        when(gitHubClient.fetchEvents(anyString(), anyString())).thenReturn(List.of(event));
        when(subscriptionRepository.findChatSubscribers(anyLong())).thenReturn(List.of(123L));

        handler.handle(link);

        verify(botClient, times(1)).sendUpdate(any());
        verify(linkRepository).updateLastUpdated(eq(1L), any());
    }
}
