package backend.academy.linktracker.scrapper.format;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.dto.response.UpdateDetails;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MessageFormatterTest {
    private final MessageFormatter formatter = new MessageFormatter();

    @Test
    @DisplayName("должен обрезать тест")
    void shouldTruncatePreviewTo200Chars() {
        String longBody = "a".repeat(300);
        UpdateDetails details = UpdateDetails.builder()
                .updateType("Test")
                .title("Title")
                .author("Author")
                .createdAt(OffsetDateTime.now())
                .preview(longBody)
                .build();

        String result = formatter.format(details);

        assertThat(result).contains("...");
    }

    @Test
    @DisplayName("не должен обрезать текст, если он короче 200 символов")
    void shouldNotTruncateShortPreview() {
        String shortBody = "Short description";
        UpdateDetails details =
                UpdateDetails.builder().updateType("Type").preview(shortBody).build();

        String result = formatter.format(details);

        assertThat(result).contains(shortBody);
        assertThat(result).doesNotContain("...");
    }
}
