package backend.academy.linktracker.scrapper.client.github;

import backend.academy.linktracker.scrapper.dto.response.GithubResponse;
import org.springframework.beans.factory.annotation.Qualifier;
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
}
