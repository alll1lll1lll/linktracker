package backend.academy.linktracker.scrapper.format;

import backend.academy.linktracker.scrapper.dto.response.UpdateDetails;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class MessageFormatter {
    private static final int MAX_PREVIEW_LENGTH = 200;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public String format(UpdateDetails details) {
        return String.format(
                "**новое обновление: %s**%nзаголовок: %s%nавтор: %s%nвремя: %s%n%nпревью:%n_%s_",
                details.getUpdateType(),
                formatTitle(details),
                formatAuthor(details),
                formatTime(details),
                formatPreview(details));
    }

    private String formatTitle(UpdateDetails details) {
        return details.getTitle() != null && !details.getTitle().isBlank() ? details.getTitle() : "без заголовка";
    }

    private String formatAuthor(UpdateDetails details) {
        return details.getAuthor() != null && !details.getAuthor().isBlank()
                ? details.getAuthor()
                : "неизвестный автор";
    }

    private String formatTime(UpdateDetails details) {
        return details.getCreatedAt() != null ? details.getCreatedAt().format(DATE_FORMATTER) : "неизвестно";
    }

    private String formatPreview(UpdateDetails details) {
        if (details.getPreview() == null) {
            return "нет текста";
        }

        String cleanPreview = details.getPreview().replaceAll("<[^>]*>", "").trim();

        if (cleanPreview.isEmpty()) {
            return "нет текста";
        }

        return cleanPreview.length() > MAX_PREVIEW_LENGTH
                ? cleanPreview.substring(0, MAX_PREVIEW_LENGTH - 3) + "..."
                : cleanPreview;
    }
}
