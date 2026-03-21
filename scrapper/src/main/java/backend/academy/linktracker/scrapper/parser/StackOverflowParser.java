package backend.academy.linktracker.scrapper.parser;

import java.net.URI;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class StackOverflowParser {
    private static final String STACKOVERFLOW_HOST = "stackoverflow.com";
    private static final String QUESTION_PATH_SEGMENT = "questions";

    public Optional<String> parseQuestionId(URI url) {
        if (url == null || !STACKOVERFLOW_HOST.equals(url.getHost())) {
            return Optional.empty();
        }

        String[] parts = url.getPath().split("/");
        if (parts.length < 3 || !QUESTION_PATH_SEGMENT.equals(parts[1])) {
            return Optional.empty();
        }
        String questionId = parts[2];
        if (!questionId.matches("\\d+")) {
            return Optional.empty();
        }

        return Optional.of(questionId);
    }
}
