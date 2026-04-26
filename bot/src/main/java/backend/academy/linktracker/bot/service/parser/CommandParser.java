package backend.academy.linktracker.bot.service.parser;

import org.springframework.stereotype.Component;

@Component
public class CommandParser {

    public String extractCommandName(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return text.trim().split("\\s+", 2)[0];
    }

    public boolean hasArguments(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String[] parts = text.trim().split("\\s+", 2);
        return parts.length > 1 && !parts[1].isBlank();
    }
}
