package backend.academy.linktracker.scrapper.client.github;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GithubEventType {
    ISSUE("IssuesEvent", "New Issue"),
    PULL_REQUEST("PullRequestEvent", "New Pull Request");

    private final String apiType;
    private final String displayName;

    public static GithubEventType fromApiType(String type) {
        return Arrays.stream(values())
                .filter(e -> e.apiType.equals(type))
                .findFirst()
                .orElse(null);
    }
}
