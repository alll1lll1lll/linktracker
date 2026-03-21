package backend.academy.linktracker.bot.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import backend.academy.linktracker.bot.dto.AddLinkRequest;
import backend.academy.linktracker.bot.dto.ListLinksResponse;
import backend.academy.linktracker.bot.dto.RemoveLinkRequest;
import backend.academy.linktracker.bot.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.bot.exception.LinkAlreadyExistsException;
import backend.academy.linktracker.bot.exception.LinkNotFoundException;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class RestScrapperClientTest {

    private RestScrapperClient scrapperClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setup() {
        RestClient.Builder builder = RestClient.builder();
        this.mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.baseUrl("http://localhost:8080").build();
        this.scrapperClient = new RestScrapperClient(restClient);
    }

    @Test
    void register_ShouldHandle409Conflict() {
        long chatId = 123L;

        mockServer
                .expect(requestTo("http://localhost:8080/tg-chat/" + chatId))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CONFLICT));

        assertThrows(ChatAlreadyExistsException.class, () -> scrapperClient.register(chatId));

        mockServer.verify();
    }

    @Test
    void addLink_ShouldSucceed() {
        long chatId = 123L;
        AddLinkRequest request = new AddLinkRequest(URI.create("https://github.com"), List.of("tech"));

        mockServer
                .expect(requestTo("http://localhost:8080/links"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Tg-Chat-Id", String.valueOf(chatId)))
                .andExpect(jsonPath("$.link").value("https://github.com"))
                .andRespond(withSuccess());

        assertDoesNotThrow(() -> scrapperClient.addLink(chatId, request));
        mockServer.verify();
    }

    @Test
    void addLink_ShouldThrowException_WhenLinkAlreadyExists() {
        long chatId = 123L;
        AddLinkRequest request = new AddLinkRequest(URI.create("https://github.com"), List.of());

        mockServer
                .expect(requestTo("http://localhost:8080/links"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CONFLICT));

        assertThrows(LinkAlreadyExistsException.class, () -> scrapperClient.addLink(chatId, request));
        mockServer.verify();
    }

    @Test
    void removeLink_ShouldThrowException_WhenLinkNotFound() {
        long chatId = 123L;
        RemoveLinkRequest request = new RemoveLinkRequest(URI.create("https://github.com"));

        mockServer
                .expect(requestTo("http://localhost:8080/links"))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header("Tg-Chat-Id", String.valueOf(chatId)))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows(LinkNotFoundException.class, () -> scrapperClient.removeLink(chatId, request));
        mockServer.verify();
    }

    @Test
    void getLinks_ShouldReturnList() {
        long chatId = 123L;
        String responseJson = """
                {
                  "links": [
                    {"id": 1, "url": "https://github.com", "tags": []}
                  ],
                  "size": 1
                }
                """;

        mockServer
                .expect(requestTo("http://localhost:8080/links"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Tg-Chat-Id", String.valueOf(chatId)))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        ListLinksResponse response = scrapperClient.getLinks(chatId);

        assertNotNull(response);
        assertEquals(1, response.getSize());
        assertEquals(
                URI.create("https://github.com"), response.getLinks().get(0).getUrl());
        mockServer.verify();
    }
}
