package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.linktracker.scrapper.client.interfaces.BotClient;
import backend.academy.linktracker.scrapper.controller.LinksController;
import backend.academy.linktracker.scrapper.controller.TgChatController;
import backend.academy.linktracker.scrapper.dto.request.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.request.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.dto.response.ListLinksResponse;
import backend.academy.linktracker.scrapper.exception.ChatNotFoundException;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {

    @DynamicPropertySource
    static void commonProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.liquibase.change-log", () -> "migrations/master.xml");
        registry.add("app.scheduler.link-update-delay", () -> "1000");
        registry.add("app.scheduler.enable", () -> "false");
        registry.add("app.client-type", () -> "rest");
        registry.add("STACKOVERFLOW_KEY", () -> "test-key");
        registry.add("STACKOVERFLOW_ACCESS_KEY", () -> "test-token");
        registry.add("GITHUB_TOKEN", () -> "test-token");
    }

    @MockitoBean
    protected BotClient botClient;

    @Autowired
    protected TgChatController tgChatController;

    @Autowired
    protected LinksController linksController;

    @Test
    void testAddAndGetLinks() {
        long chatId = 1L;
        URI linkUrl = URI.create("https://github.com/test/test");
        AddLinkRequest addRequest = new AddLinkRequest(linkUrl, List.of("java"));

        tgChatController.register(chatId);
        linksController.addLink(chatId, addRequest);
        ListLinksResponse response = linksController.getLinks(chatId);

        assertThat(response.getSize()).isEqualTo(1);
        assertThat(response.getLinks().get(0).getUrl()).isEqualTo(linkUrl);
    }

    @Test
    void testDeleteLink() {
        long chatId = 2L;
        URI linkUrl = URI.create("https://stackoverflow.com/questions/1");
        AddLinkRequest addRequest = new AddLinkRequest(linkUrl, List.of());
        RemoveLinkRequest removeRequest = new RemoveLinkRequest(linkUrl);

        tgChatController.register(chatId);
        linksController.addLink(chatId, addRequest);
        linksController.deleteLink(chatId, removeRequest);
        ListLinksResponse response = linksController.getLinks(chatId);

        assertThat(response.getSize()).isEqualTo(0);
    }

    @Test
    void testDeleteLinkFromNonExistentChat() {
        long chatId = 3L;
        long fakeChatId = 999L;
        URI linkUrl = URI.create("https://github.com/test");
        AddLinkRequest addRequest = new AddLinkRequest(linkUrl, List.of());
        RemoveLinkRequest removeRequest = new RemoveLinkRequest(linkUrl);

        tgChatController.register(chatId);
        linksController.addLink(chatId, addRequest);

        assertThatThrownBy(() -> linksController.deleteLink(fakeChatId, removeRequest))
                .isInstanceOf(ChatNotFoundException.class);
    }

    @Test
    void testAddLinkToNonExistentChat() {
        long fakeChatId = 444L;
        URI linkUrl = URI.create("https://github.com/test");
        AddLinkRequest addRequest = new AddLinkRequest(linkUrl, List.of());

        assertThatThrownBy(() -> linksController.addLink(fakeChatId, addRequest))
                .isInstanceOf(ChatNotFoundException.class);
    }

    @Test
    void testChatLifecycle() {
        long chatId = 5L;
        tgChatController.register(chatId);
        tgChatController.deleteChat(chatId);

        URI linkUrl = URI.create("https://github.com/test");
        AddLinkRequest addRequest = new AddLinkRequest(linkUrl, List.of());

        assertThatThrownBy(() -> linksController.addLink(chatId, addRequest)).isInstanceOf(ChatNotFoundException.class);
    }

    @Test
    void testDeleteNonExistentChat() {
        long fakeChatId = 666L;
        assertThatThrownBy(() -> tgChatController.deleteChat(fakeChatId)).isInstanceOf(ChatNotFoundException.class);
    }
}
