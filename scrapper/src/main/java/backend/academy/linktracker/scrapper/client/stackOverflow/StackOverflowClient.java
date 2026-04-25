package backend.academy.linktracker.scrapper.client.stackOverflow;

import backend.academy.linktracker.scrapper.dto.response.stackoverflow.StackOverflowAnswerResponse;
import backend.academy.linktracker.scrapper.dto.response.stackoverflow.StackOverflowCommentResponse;
import backend.academy.linktracker.scrapper.dto.response.stackoverflow.StackOverflowResponse;
import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class StackOverflowClient {
    private final RestClient restClient;
    private final StackoverflowProperties properties;

    public StackOverflowClient(
            @Qualifier("stackOverflowRestClient") RestClient restClient, StackoverflowProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public StackOverflowResponse fetchQuestion(String questionId) {
        return restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/questions/{id}")
                        .queryParam("site", "stackoverflow")
                        .build(questionId))
                .retrieve()
                .body(StackOverflowResponse.class);
    }

    public StackOverflowAnswerResponse fetchAnswers(String questionId, OffsetDateTime since) {
        return restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/questions/{id}/answers")
                        .queryParam("site", "stackoverflow")
                        .queryParam("order", "desc")
                        .queryParam("sort", "creation")
                        .queryParam("fromdate", since.toEpochSecond())
                        .queryParam("filter", "withbody")
                        .build(questionId))
                .retrieve()
                .body(StackOverflowAnswerResponse.class);
    }

    public StackOverflowCommentResponse fetchComments(String questionId, OffsetDateTime since) {
        return restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/questions/{id}/comments")
                        .queryParam("site", "stackoverflow")
                        .queryParam("order", "desc")
                        .queryParam("sort", "creation")
                        .queryParam("fromdate", since.toEpochSecond())
                        .queryParam("filter", "withbody")
                        .build(questionId))
                .retrieve()
                .body(StackOverflowCommentResponse.class);
    }
}
