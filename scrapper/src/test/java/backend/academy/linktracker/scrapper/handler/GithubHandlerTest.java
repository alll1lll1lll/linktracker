package backend.academy.linktracker.scrapper.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.scrapper.client.github.GitHubClient;
import backend.academy.linktracker.scrapper.client.handler.GithubHandler;
import backend.academy.linktracker.scrapper.client.interfaces.BotClient;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.dto.response.GithubResponse;
import backend.academy.linktracker.scrapper.model.LinkModel;
import backend.academy.linktracker.scrapper.parser.GitHubParser;
import backend.academy.linktracker.scrapper.parser.RepositoryInfo;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GithubHandlerTest {

    @Mock
    private GitHubClient gitHubClient;

    @Mock
    private BotClient botClient;

    @Mock
    private GitHubParser parser;

    @InjectMocks
    private GithubHandler ghHandler;

    @BeforeEach
    void setUp() {
        when(parser.parseRepositoryInfo(any(URI.class))).thenReturn(Optional.of(new RepositoryInfo("user", "repo")));
    }

    @Test
    void handle() {
        LinkModel link = new LinkModel(
                10L,
                URI.create("https://github.com/user/repo"),
                OffsetDateTime.now().minusDays(1),
                Map.of(
                        111L, List.of("tag1"),
                        222L, Collections.emptyList()));
        GithubResponse response = mock(GithubResponse.class);
        when(response.getPushedAt()).thenReturn(OffsetDateTime.now());
        when(gitHubClient.fetchRepoInfo(anyString(), anyString())).thenReturn(response);

        ghHandler.handle(link);

        ArgumentCaptor<LinkUpdate> captor = ArgumentCaptor.forClass(LinkUpdate.class);
        verify(botClient).sendUpdate(captor.capture());

        LinkUpdate sentUpdate = captor.getValue();

        assertThat(sentUpdate.getTgChatIds()).containsExactlyInAnyOrder(111L, 222L);
        assertThat(sentUpdate.getTgChatIds()).doesNotContain(333L);
    }

    @Test
    void handle_ShouldDoNothing_WhenLinkCannotBeParsed() {
        when(parser.parseRepositoryInfo(any(URI.class))).thenReturn(Optional.empty());
        LinkModel link = new LinkModel(1L, URI.create("https://not-github.com"), OffsetDateTime.now(), Map.of());

        ghHandler.handle(link);

        verifyNoInteractions(gitHubClient);
        verifyNoInteractions(botClient);
    }

    @Test
    void handle_ShouldNotSendUpdate_WhenNoSubscribers() {
        LinkModel link = new LinkModel(
                1L, URI.create("https://github.com/a/b"), OffsetDateTime.now().minusDays(1), Map.of());
        GithubResponse response = mock(GithubResponse.class);
        when(response.getPushedAt()).thenReturn(OffsetDateTime.now());
        when(gitHubClient.fetchRepoInfo(anyString(), anyString())).thenReturn(response);

        ghHandler.handle(link);

        verify(botClient, never()).sendUpdate(any());
    }
}
