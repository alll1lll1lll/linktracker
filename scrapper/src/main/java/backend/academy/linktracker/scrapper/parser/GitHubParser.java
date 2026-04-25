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

        String path = url.getPath().replaceAll("^/|/$", "");
        String[] parts = path.split("/");

        if (parts.length != 2) {
            return Optional.empty();
        }

        String owner = parts[0];
        String repo = parts[1];

        if (owner.isBlank() || repo.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(new RepositoryInfo(owner, repo));
    }
}
