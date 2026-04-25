package backend.academy.linktracker.scrapper.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.scrapper.client.interfaces.BotClient;
import backend.academy.linktracker.scrapper.client.stackOverflow.StackOverflowClient;
import backend.academy.linktracker.scrapper.dto.response.stackoverflow.*;
import backend.academy.linktracker.scrapper.format.MessageFormatter;
import backend.academy.linktracker.scrapper.model.LinkModel;
import backend.academy.linktracker.scrapper.parser.StackOverflowParser;
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
class StackOverflowHandlerTest {

    @Mock
    private StackOverflowClient client;

    @Mock
    private BotClient botClient;

    @Mock
    private StackOverflowParser parser;

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    private StackOverflowHandler handler;

    @BeforeEach
    void setUp() {
        handler = new StackOverflowHandler(
                botClient, client, parser, linkRepository, subscriptionRepository, new MessageFormatter());
    }

    @Test
    void shouldHandleBothAnswersAndComments() {
        LinkModel link = new LinkModel(
                1L,
                URI.create("https://stackoverflow.com/questions/1"),
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().minusDays(1));
        long now = OffsetDateTime.now().toEpochSecond();

        StackOverflowResponse qResp = new StackOverflowResponse();
        StackOverflowResponse.StackOverflowItem item = new StackOverflowResponse.StackOverflowItem();
        qResp.setItems(List.of(item));

        StackOverflowAnswerResponse aResp =
                new StackOverflowAnswerResponse(List.of(new StackOverflowAnswerResponse.StackOverflowAnswerItemResponse(
                        new StackOverflowAnswerResponse.StackOverflowOwnerResponse("ne shisha"), now, "Answer Body")));

        StackOverflowCommentResponse cResp = new StackOverflowCommentResponse(
                List.of(new StackOverflowCommentResponse.StackOverflowCommentItemResponse(
                        new StackOverflowCommentResponse.StackOverflowOwnerResponse("ne spesha"),
                        now,
                        "Comment Body")));

        when(parser.parseQuestionId(any())).thenReturn(Optional.of("1"));
        when(client.fetchQuestion(anyString())).thenReturn(qResp);
        when(client.fetchAnswers(anyString(), any(OffsetDateTime.class))).thenReturn(aResp);
        when(client.fetchComments(anyString(), any(OffsetDateTime.class))).thenReturn(cResp);
        when(subscriptionRepository.findChatSubscribers(1L)).thenReturn(List.of(123L));

        handler.handle(link);

        verify(botClient, times(2)).sendUpdate(any());
        verify(linkRepository).updateLastUpdated(eq(1L), any());
    }
}
