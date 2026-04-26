package backend.academy.linktracker.scrapper.dto.response.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GithubEventResponse {
    private String type;
    private GithubActorResponse actor;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    private GithubPayloadResponse payload;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GithubActorResponse {
        private String login;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GithubPayloadResponse {
        private String action;
        private GithubIssueResponse issue;

        @JsonProperty("pull_request")
        private GithubPullRequestResponse pullRequest;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GithubPullRequestResponse {
        private String title;
        private String body;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GithubIssueResponse {
        private String title;
        private String body;
    }
}
