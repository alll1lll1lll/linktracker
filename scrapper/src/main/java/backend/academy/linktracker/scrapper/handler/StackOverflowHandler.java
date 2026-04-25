package backend.academy.linktracker.scrapper.handler;

import backend.academy.linktracker.scrapper.client.interfaces.BotClient;
import backend.academy.linktracker.scrapper.client.stackOverflow.StackOverflowClient;
import backend.academy.linktracker.scrapper.dto.response.UpdateDetails;
import backend.academy.linktracker.scrapper.format.MessageFormatter;
import backend.academy.linktracker.scrapper.model.LinkModel;
import backend.academy.linktracker.scrapper.parser.StackOverflowParser;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StackOverflowHandler extends AbstractLinkHandler {
    private final StackOverflowClient client;
    private final StackOverflowParser parser;

    private static final String HOST = "stackoverflow.com";
    private static final String DEFAULT_QUESTION_TITLE = "вопрос на StackOverflow";
    private static final String NEW_ANSWER_TYPE = "новый ответ";
    private static final String NEW_COMMENT_TYPE = "новый комментарий";
    private static final String UNKNOWN = "Unknown";

    public StackOverflowHandler(
            BotClient botClient,
            StackOverflowClient client,
            StackOverflowParser parser,
            LinkRepository linkRepository,
            SubscriptionRepository subscriptionRepository,
            MessageFormatter messageFormatter) {
        super(botClient, subscriptionRepository, linkRepository, messageFormatter);
        this.client = client;
        this.parser = parser;
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    protected List<UpdateDetails> fetchUpdatesSince(LinkModel link, OffsetDateTime since) {
        String questionId = parser.parseQuestionId(link.getUrl()).orElse(null);
        if (questionId == null) {
            return List.of();
        }

        var questionResponse = client.fetchQuestion(questionId);
        String questionTitle = (questionResponse != null
                        && questionResponse.getItems() != null
                        && !questionResponse.getItems().isEmpty())
                ? questionResponse.getItems().get(0).getTitle()
                : DEFAULT_QUESTION_TITLE;

        var answersStream = fetchNewAnswers(questionId, since, questionTitle);
        var commentsStream = fetchNewComments(questionId, since, questionTitle);

        return Stream.concat(answersStream, commentsStream).toList();
    }

    private Stream<UpdateDetails> fetchNewAnswers(String questionId, OffsetDateTime since, String questionTitle) {
        return client.fetchAnswers(questionId, since).getItems().stream()
                .filter(answer -> getOffsetDateTime(answer.getCreationDate()).isAfter(since))
                .map(answer -> UpdateDetails.builder()
                        .updateType(NEW_ANSWER_TYPE)
                        .title(questionTitle)
                        .author(answer.getOwner() != null ? answer.getOwner().getDisplayName() : UNKNOWN)
                        .createdAt(getOffsetDateTime(answer.getCreationDate()))
                        .preview(answer.getBody())
                        .build());
    }

    private Stream<UpdateDetails> fetchNewComments(String questionId, OffsetDateTime since, String questionTitle) {
        return client.fetchComments(questionId, since).getItems().stream()
                .filter(comment -> getOffsetDateTime(comment.getCreationDate()).isAfter(since))
                .map(comment -> UpdateDetails.builder()
                        .updateType(NEW_COMMENT_TYPE)
                        .title(questionTitle)
                        .author(comment.getOwner() != null ? comment.getOwner().getDisplayName() : UNKNOWN)
                        .createdAt(getOffsetDateTime(comment.getCreationDate()))
                        .preview(comment.getBody())
                        .build());
    }

    private OffsetDateTime getOffsetDateTime(long unixTimeSeconds) {
        return Instant.ofEpochSecond(unixTimeSeconds).atOffset(ZoneOffset.UTC);
    }
}
