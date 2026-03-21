package backend.academy.linktracker.bot.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class TagService {

    public List<String> parseMultipleTags(String text) {
        String trimmed = text.trim();
        if (trimmed.equals("-") || trimmed.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(trimmed.split("[,\\s]+"))
                .filter(tag -> !tag.isBlank())
                .collect(Collectors.toList());
    }

    public Optional<String> extractTagFromCommand(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        String[] parts = text.trim().split("\\s+");
        return parts.length > 1 ? Optional.of(parts[1]) : Optional.empty();
    }
}
