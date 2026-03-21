package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

@SpringBootTest
class ScrapperIntegrationTest {

    @Autowired
    private TgChatController tgChatController;

    @Autowired
    private LinksController linksController;

    @Test
    void test3_1() {
        long chatId = 1L;
        URI linkUrl = URI.create("https://github.com/test/test");
        AddLinkRequest addRequest = new AddLinkRequest(linkUrl, List.of("j"));

        tgChatController.register(chatId);
        linksController.addLink(chatId, addRequest);
        ListLinksResponse response = linksController.getLinks(chatId);

        assertThat(response.getSize()).isEqualTo(1);
        assertThat(response.getLinks().get(0).getUrl()).isEqualTo(linkUrl);
    }

    @Test
    void test3_2() {
        long chatId = 2L;
        URI linkUrl = URI.create("https://stackoverflow.com/questions/1");
        AddLinkRequest addRequest = new AddLinkRequest(linkUrl, List.of());
        RemoveLinkRequest removeRequest = new RemoveLinkRequest(linkUrl);

        tgChatController.register(chatId);
        linksController.addLink(chatId, addRequest);
        linksController.deleteLink(chatId, removeRequest);
        ListLinksResponse response = linksController.getLinks(chatId);

        assertThat(response.getSize()).isEqualTo(0);
        assertThat(response.getLinks()).isEmpty();
    }

    @Test
    void test3_3() {
        long chatId = 3L;
        long fakeChatId = 999L;
        URI linkUrl = URI.create("https://github.com/test");
        AddLinkRequest addRequest = new AddLinkRequest(linkUrl, List.of());
        RemoveLinkRequest removeRequest = new RemoveLinkRequest(linkUrl);

        tgChatController.register(chatId);
        linksController.addLink(chatId, addRequest);

        assertThatThrownBy(() -> linksController.deleteLink(fakeChatId, removeRequest))
                .isInstanceOf(ChatNotFoundException.class);

        ListLinksResponse response = linksController.getLinks(chatId);
        assertThat(response.getSize()).isEqualTo(1);
    }

    @Test
    void test3_4() {
        long fakeChatId = 444L;
        URI linkUrl = URI.create("https://github.com/test");
        AddLinkRequest addRequest = new AddLinkRequest(linkUrl, List.of());

        assertThatThrownBy(() -> linksController.addLink(fakeChatId, addRequest))
                .isInstanceOf(ChatNotFoundException.class);
    }

    @Test
    void test3_5() {
        long chatId = 5L;
        URI linkUrl = URI.create("https://github.com/test");
        AddLinkRequest addRequest = new AddLinkRequest(linkUrl, List.of());

        tgChatController.register(chatId);

        tgChatController.deleteChat(chatId);

        assertThatThrownBy(() -> linksController.addLink(chatId, addRequest)).isInstanceOf(ChatNotFoundException.class);
    }

    @Test
    void test3_6() {
        long fakeChatId = 666L;

        assertThatThrownBy(() -> tgChatController.deleteChat(fakeChatId)).isInstanceOf(ChatNotFoundException.class);
    }
}
