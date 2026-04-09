package backend.academy.linktracker.scrapper.handler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.client.handler.StackOverflowHandler;
import backend.academy.linktracker.scrapper.client.interfaces.BotClient;
import backend.academy.linktracker.scrapper.client.stackOverflow.StackOverflowClient;
import backend.academy.linktracker.scrapper.model.LinkModel;
import backend.academy.linktracker.scrapper.parser.StackOverflowParser;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import backend.academy.linktracker.scrapper.service.LinkUpdateService;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@ExtendWith(MockitoExtension.class)
class StackOverflowHandlerTest {

    @Mock
    private StackOverflowClient stackOverflowClient;

    @Mock
    private BotClient botClient;

    @Mock
    private StackOverflowParser stackOverflowUrlParser;

    private StackOverflowHandler stackOverflowHandler;
    private LinkUpdateService linkUpdateService;
    private LinkRepository linkRepository;
    private SubscriptionRepository subscriptionRepository;

    @BeforeEach
    void setUp() {
        stackOverflowHandler = new StackOverflowHandler(
                botClient, stackOverflowClient, stackOverflowUrlParser, linkRepository, subscriptionRepository);
        linkUpdateService = new LinkUpdateService(List.of(stackOverflowHandler));
        when(stackOverflowUrlParser.parseQuestionId(any(URI.class))).thenReturn(Optional.of("12345"));
    }

    @Test
    void shouldNotCrashWhenStackOverflowApiReturnsError() {
        LinkModel link = new LinkModel();
        link.setId(1L);
        link.setUrl(URI.create("https://stackoverflow.com/questions/12345/some-title"));

        when(stackOverflowClient.fetchQuestion(anyString()))
                .thenThrow(new HttpClientErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        assertDoesNotThrow(() -> linkUpdateService.processUpdate(link));
    }

    @Test
    void shouldNotCrashWhenInvalid() {
        LinkModel link = new LinkModel();
        link.setId(2L);
        link.setUrl(URI.create("https://stackoverflow.com/questions/67890/another-title"));

        when(stackOverflowClient.fetchQuestion(anyString()))
                .thenThrow(new RuntimeException("JSON parse error: items is null"));

        assertDoesNotThrow(() -> linkUpdateService.processUpdate(link));
    }
}
