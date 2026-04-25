package backend.academy.linktracker.scrapper.client.github;

import backend.academy.linktracker.scrapper.dto.response.github.GithubEventResponse;
import backend.academy.linktracker.scrapper.dto.response.github.GithubResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GitHubClient {
    private final RestClient restClient;

    public GitHubClient(@Qualifier("githubRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public GithubResponse fetchRepoInfo(String owner, String repo) {
        return restClient
                .get()
                .uri("/repos/{owner}/{repo}", owner, repo)
                .retrieve()
                .body(GithubResponse.class);
    }

    public List<GithubEventResponse> fetchEvents(String owner, String repo) {
        return restClient
                .get()
                .uri("/repos/{owner}/{repo}/events?per_page=30", owner, repo)
                .retrieve()
                .body(new ParameterizedTypeReference<List<GithubEventResponse>>() {});
    }
}
