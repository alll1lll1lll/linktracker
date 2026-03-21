package backend.academy.linktracker.scrapper.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import backend.academy.linktracker.scrapper.client.stackOverflow.StackOverflowClient;
import backend.academy.linktracker.scrapper.dto.response.StackOverflowItem;
import backend.academy.linktracker.scrapper.dto.response.StackOverflowResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {StackOverflowClient.class, TestClientConfig.class})
class StackOverflowClientTest {

    private WireMockServer wireMockServer;

    @Autowired
    private StackOverflowClient stackOverflowClient;

    @BeforeEach
    void setup() {
        wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
        configureFor("localhost", 8080);
    }

    @AfterEach
    void teardown() {
        wireMockServer.stop();
    }

    @Test
    void shouldFetchQuestion() {
        String dateStr = "2025-11-01T10:00:00Z";
        Long questionId = 123L;

        stubFor(get(urlPathEqualTo("/questions/" + questionId))
                .withQueryParam("site", equalTo("stackoverflow"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"items\": [{\"last_activity_date\": \"" + dateStr + "\", \"question_id\": "
                                + questionId + "}]}")));

        StackOverflowResponse response = stackOverflowClient.fetchQuestion(String.valueOf(questionId));
        assertNotNull(response);
        assertFalse(response.getItems().isEmpty(), "список пуст!");

        StackOverflowItem item = response.getItems().get(0);
        assertEquals(questionId, item.getQuestionId());
        assertEquals(OffsetDateTime.parse(dateStr), item.getLastActivityDate());
    }
}
