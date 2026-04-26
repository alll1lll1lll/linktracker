package backend.academy.linktracker.bot.service;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.bot.service.tracking.TagService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TagServiceTest {

    private final TagService tagService = new TagService();

    @Test
    @DisplayName("разбор нескольких тегов, разделенных запятыми и пробелами")
    void parseMultipleTags_commaAndSpaceSeparated() {
        String text = "tag1, tag2,tag3 , tag4";
        List<String> expectedTags = List.of("tag1", "tag2", "tag3", "tag4");

        List<String> actualTags = tagService.parseMultipleTags(text);

        assertThat(actualTags).containsExactlyInAnyOrderElementsOf(expectedTags);
    }

    @Test
    @DisplayName("разбор нескольких тегов, разделенных только пробелами")
    void parseMultipleTags_spaceSeparated() {
        String text = "tag1 tag2 tag3";
        List<String> expectedTags = List.of("tag1", "tag2", "tag3");

        List<String> actualTags = tagService.parseMultipleTags(text);

        assertThat(actualTags).containsExactlyInAnyOrderElementsOf(expectedTags);
    }

    @Test
    @DisplayName("разбор одного тега")
    void parseMultipleTags_singleTag() {
        String text = "onlytag";
        List<String> expectedTags = List.of("onlytag");

        List<String> actualTags = tagService.parseMultipleTags(text);

        assertThat(actualTags).containsExactlyInAnyOrderElementsOf(expectedTags);
    }

    @Test
    @DisplayName("разбор пустого текста")
    void parseMultipleTags_emptyText() {
        String text = "";

        List<String> actualTags = tagService.parseMultipleTags(text);

        assertThat(actualTags).isEmpty();
    }

    @Test
    @DisplayName("разбор текста только с пробелами")
    void parseMultipleTags_blankText() {
        String text = "   ";

        List<String> actualTags = tagService.parseMultipleTags(text);

        assertThat(actualTags).isEmpty();
    }

    @Test
    @DisplayName("разбор текста, содержащего только дефис")
    void parseMultipleTags_dashText() {
        String text = "-";

        List<String> actualTags = tagService.parseMultipleTags(text);

        assertThat(actualTags).isEmpty();
    }

    @Test
    @DisplayName("разбор текста с повторяющимися разделителями")
    void parseMultipleTags_multipleDelimiters() {
        String text = "tag1,,  tag2 ,,,tag3";
        List<String> expectedTags = List.of("tag1", "tag2", "tag3");

        List<String> actualTags = tagService.parseMultipleTags(text);

        assertThat(actualTags).containsExactlyInAnyOrderElementsOf(expectedTags);
    }

    @Test
    @DisplayName("извлечение тега из команды с одним аргументом")
    void extractTagFromCommand_singleArgument() {
        String text = "/addtag mytag";

        Optional<String> actualTag = tagService.extractTagFromCommand(text);

        assertThat(actualTag).contains("mytag");
    }

    @Test
    @DisplayName("извлечение тега из команды с несколькими аргументами (первый аргумент)")
    void extractTagFromCommand_multipleArguments() {
        String text = "/addtag mytag otherarg";

        Optional<String> actualTag = tagService.extractTagFromCommand(text);

        assertThat(actualTag).contains("mytag");
    }

    @Test
    @DisplayName("извлечение тега из команды без аргументов")
    void extractTagFromCommand_noArguments() {
        String text = "/addtag";

        Optional<String> actualTag = tagService.extractTagFromCommand(text);

        assertThat(actualTag).isEmpty();
    }

    @Test
    @DisplayName("извлечение тега из пустого текста")
    void extractTagFromCommand_emptyText() {
        String text = "";

        Optional<String> actualTag = tagService.extractTagFromCommand(text);

        assertThat(actualTag).isEmpty();
    }

    @Test
    @DisplayName("извлечение тега из текста только с пробелами")
    void extractTagFromCommand_blankText() {
        String text = "   ";

        Optional<String> actualTag = tagService.extractTagFromCommand(text);

        assertThat(actualTag).isEmpty();
    }
}
