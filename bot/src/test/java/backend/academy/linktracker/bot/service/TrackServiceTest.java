package backend.academy.linktracker.bot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.dto.AddLinkRequest;
import backend.academy.linktracker.bot.dto.RemoveLinkRequest;
import backend.academy.linktracker.bot.enumResponse.ResponseCode;
import backend.academy.linktracker.bot.exception.LinkAlreadyExistsException;
import backend.academy.linktracker.bot.exception.LinkNotFoundException;
import backend.academy.linktracker.bot.exception.ServiceUnavailableException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrackServiceTest {

    @Mock
    private ScrapperClient scrapperClient;

    @InjectMocks
    private TrackService trackService;

    private final long CHAT_ID = 1L;
    private final URI TEST_URI;
    private final String TEST_URI_STRING = "http://example.com";
    private final List<String> TEST_TAGS = List.of("tag1", "tag2");

    public TrackServiceTest() throws URISyntaxException {
        TEST_URI = new URI(TEST_URI_STRING);
    }

    @Test
    @DisplayName("успешное отслеживание ссылки")
    void track_success() {
        ResponseCode responseCode = trackService.track(CHAT_ID, TEST_URI, TEST_TAGS);

        assertThat(responseCode).isEqualTo(ResponseCode.TRACK_SUCCESS);
        verify(scrapperClient).addLink(eq(CHAT_ID), any(AddLinkRequest.class));
    }

    @Test
    @DisplayName("обработка LinkAlreadyExistsException")
    void track_linkAlreadyExists() {
        doThrow(LinkAlreadyExistsException.class).when(scrapperClient).addLink(eq(CHAT_ID), any(AddLinkRequest.class));

        ResponseCode responseCode = trackService.track(CHAT_ID, TEST_URI, TEST_TAGS);

        assertThat(responseCode).isEqualTo(ResponseCode.ALREADY_TRACKED);
        verify(scrapperClient).addLink(eq(CHAT_ID), any(AddLinkRequest.class));
    }

    @Test
    @DisplayName("обработка ServiceUnavailableException")
    void track_serviceUnavailable() {
        doThrow(ServiceUnavailableException.class).when(scrapperClient).addLink(eq(CHAT_ID), any(AddLinkRequest.class));

        ResponseCode responseCode = trackService.track(CHAT_ID, TEST_URI, TEST_TAGS);

        assertThat(responseCode).isEqualTo(ResponseCode.ERROR_ADD);
        verify(scrapperClient).addLink(eq(CHAT_ID), any(AddLinkRequest.class));
    }

    @Test
    @DisplayName("обработка общего исключения")
    void track_generalException() {
        doThrow(RuntimeException.class).when(scrapperClient).addLink(eq(CHAT_ID), any(AddLinkRequest.class));

        ResponseCode responseCode = trackService.track(CHAT_ID, TEST_URI, TEST_TAGS);

        assertThat(responseCode).isEqualTo(ResponseCode.ERROR_ADD);
        verify(scrapperClient).addLink(eq(CHAT_ID), any(AddLinkRequest.class));
    }

    @Test
    @DisplayName("успешное удаление ссылки")
    void untrack_success() {
        ResponseCode responseCode = trackService.untrack(CHAT_ID, TEST_URI_STRING);

        assertThat(responseCode).isEqualTo(ResponseCode.UNTRACK_SUCCESS);
        verify(scrapperClient).removeLink(eq(CHAT_ID), any(RemoveLinkRequest.class));
    }

    @Test
    @DisplayName("обработка LinkNotFoundException")
    void untrack_linkNotFound() {
        doThrow(LinkNotFoundException.class).when(scrapperClient).removeLink(eq(CHAT_ID), any(RemoveLinkRequest.class));

        ResponseCode responseCode = trackService.untrack(CHAT_ID, TEST_URI_STRING);

        assertThat(responseCode).isEqualTo(ResponseCode.NOT_FOUND);
        verify(scrapperClient).removeLink(eq(CHAT_ID), any(RemoveLinkRequest.class));
    }

    @Test
    @DisplayName("обработка ServiceUnavailableException")
    void untrack_serviceUnavailable() {
        doThrow(ServiceUnavailableException.class)
                .when(scrapperClient)
                .removeLink(eq(CHAT_ID), any(RemoveLinkRequest.class));

        ResponseCode responseCode = trackService.untrack(CHAT_ID, TEST_URI_STRING);

        assertThat(responseCode).isEqualTo(ResponseCode.ERROR_UNTRACK);
        verify(scrapperClient).removeLink(eq(CHAT_ID), any(RemoveLinkRequest.class));
    }

    @Test
    @DisplayName("обработка общего исключения")
    void untrack_generalException() {
        doThrow(RuntimeException.class).when(scrapperClient).removeLink(eq(CHAT_ID), any(RemoveLinkRequest.class));

        ResponseCode responseCode = trackService.untrack(CHAT_ID, TEST_URI_STRING);

        assertThat(responseCode).isEqualTo(ResponseCode.ERROR_UNTRACK);
        verify(scrapperClient).removeLink(eq(CHAT_ID), any(RemoveLinkRequest.class));
    }
}
