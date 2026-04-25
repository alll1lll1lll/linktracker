package backend.academy.linktracker.scrapper.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import backend.academy.linktracker.scrapper.client.github.GitHubClient;
import backend.academy.linktracker.scrapper.dto.response.github.GithubResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {GitHubClient.class, TestClientConfig.class})
public class GithubClientTest {

    private WireMockServer wireMockServer;

    @Autowired
    private GitHubClient gitHubClient;

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
    void shouldFetchRepoInfo() {
        stubFor(get(urlEqualTo("/repos/owner/repo"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"updated_at\": \"2025-11-11T10:00:00Z\"}")));

        GithubResponse response = gitHubClient.fetchRepoInfo("owner", "repo");

        assertNotNull(response);
        assertNotNull(response.getUpdatedAt());
    }
}
