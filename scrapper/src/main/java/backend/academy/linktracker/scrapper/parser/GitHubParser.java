package backend.academy.linktracker.scrapper.parser;

import java.net.URI;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class GitHubParser {
    private static final String GITHUB_HOST = "github.com";

    public Optional<RepositoryInfo> parseRepositoryInfo(URI url) {
        if (url == null || !GITHUB_HOST.equals(url.getHost())) {
            return Optional.empty();
        }
        String[] parts = url.getPath().split("/");
        if (parts.length < 3) {
            return Optional.empty();
        }
        String owner = parts[1];
        String repo = parts[2];
        if (owner.isBlank() || repo.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new RepositoryInfo(owner, repo));
    }
}
