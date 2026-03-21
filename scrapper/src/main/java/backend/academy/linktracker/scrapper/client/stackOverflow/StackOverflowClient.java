package backend.academy.linktracker.scrapper.client.stackOverflow;

import backend.academy.linktracker.scrapper.dto.response.StackOverflowResponse;
import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
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
}
