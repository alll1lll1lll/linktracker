package backend.academy.linktracker.bot.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.dto.LinkResponse;
import backend.academy.linktracker.bot.dto.ListLinksResponse;
import backend.academy.linktracker.bot.service.ResponseFormatter;
import backend.academy.linktracker.bot.service.TagService;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ListCommandTest {

    @Mock
    private ScrapperClient scrapperClient;

    @Mock
    private Update update;

    private TagService tagService;
    private ListCommand listCommand;
    private ResponseFormatter responseFormatter;
    private final long chatId = 123L;

    @BeforeEach
    void setUp() {
        tagService = new TagService();
        responseFormatter = new ResponseFormatter();
        listCommand = new ListCommand(scrapperClient, tagService, responseFormatter);
    }

    @Test
    void empty() {
        ListLinksResponse emptyResponse = new ListLinksResponse(List.of(), 0);
        when(scrapperClient.getLinks(chatId)).thenReturn(emptyResponse);

        SendMessage response = listCommand.handle(update, chatId, "/list");

        String text = (String) response.getParameters().get("text");
        assertThat(text).isEqualTo("у вас нет активных подписок.");
    }

    @Test
    void listLinks() {
        LinkResponse l1 = new LinkResponse(1L, URI.create("https://github.com/alll1lll1lll/web1"), List.of());
        LinkResponse l2 = new LinkResponse(2L, URI.create("https://stackoverflow.com"), List.of("help"));
        ListLinksResponse listResponse = new ListLinksResponse(List.of(l1, l2), 2);
        when(scrapperClient.getLinks(chatId)).thenReturn(listResponse);
        String expectedText = """
                Ваши ссылки:
                - https://github.com/alll1lll1lll/web1
                - https://stackoverflow.com [help]
                """;

        SendMessage response = listCommand.handle(update, chatId, "/list");

        String text = (String) response.getParameters().get("text");
        assertThat(text.trim()).isEqualTo(expectedText.trim());
    }

    @Test
    void listLinksWithTagFilter() {
        LinkResponse l1 = new LinkResponse(1L, URI.create("https://github.com/repo1"), List.of("java", "work"));
        LinkResponse l2 = new LinkResponse(2L, URI.create("https://github.com/repo2"), List.of("python"));
        ListLinksResponse listResponse = new ListLinksResponse(List.of(l1, l2), 2);
        when(scrapperClient.getLinks(chatId)).thenReturn(listResponse);
        String expectedText = """
                Ваши ссылки с тегом java:
                - https://github.com/repo1 [java, work]
                """;

        SendMessage response = listCommand.handle(update, chatId, "/list java");

        String text = (String) response.getParameters().get("text");
        assertThat(text.trim()).isEqualTo(expectedText.trim());
    }

    @Test
    void listLinksWithTagFilterEmpty() {
        LinkResponse l1 = new LinkResponse(1L, URI.create("https://github.com/repo1"), List.of("java"));
        ListLinksResponse listResponse = new ListLinksResponse(List.of(l1), 1);
        when(scrapperClient.getLinks(chatId)).thenReturn(listResponse);

        SendMessage response = listCommand.handle(update, chatId, "/list golang");

        String text = (String) response.getParameters().get("text");
        assertThat(text).isEqualTo("у вас нет активных подписок с тегом: golang");
    }

    @Test
    void exc() {
        when(scrapperClient.getLinks(chatId)).thenThrow(new RuntimeException("API error"));

        SendMessage response = listCommand.handle(update, chatId, "/list");

        String text = (String) response.getParameters().get("text");
        assertThat(text).isEqualTo("произошла ошибка получения списка. пожалуйста, попробуйте позже.");
    }
}
