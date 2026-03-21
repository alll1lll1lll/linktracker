package backend.academy.linktracker.scrapper.parser;

public record RepositoryInfo(String owner, String repo) {
    public String fullName() {
        return owner + "/" + repo;
    }
}
