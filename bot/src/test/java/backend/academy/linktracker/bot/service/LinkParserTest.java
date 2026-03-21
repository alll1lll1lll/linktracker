package backend.academy.linktracker.bot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.enumResponse.ResponseCode;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LinkParserTest {

    @Mock
    private MessageService messageService;

    @InjectMocks
    private LinkParser linkParser;

    @BeforeEach
    void setUp() {
        when(messageService.getMessage(ResponseCode.INVALID_LINK_FORMAT)).thenReturn("Некорректный формат ссылки.");
    }

    @Test
    @DisplayName("успешный разбор валидной ссылки")
    void parse_validLink() throws URISyntaxException {
        String urlString = "https://github.com/user/repo";
        URI expectedUri = new URI(urlString);

        URI actualUri = linkParser.parse(urlString);

        assertThat(actualUri).isEqualTo(expectedUri);
    }

    @Test
    @DisplayName("разбор ссылки с поддоменом")
    void parse_validLinkWithSubdomain() throws URISyntaxException {
        String urlString = "https://docs.github.com/en";
        URI expectedUri = new URI(urlString);
        URI actualUri = linkParser.parse(urlString);

        assertThat(actualUri).isEqualTo(expectedUri);
    }

    @Test
    @DisplayName("разбор HTTP ссылки")
    void parse_httpLink() throws URISyntaxException {
        String urlString = "http://stackoverflow.com/questions/123";
        URI expectedUri = new URI(urlString);

        URI actualUri = linkParser.parse(urlString);

        assertThat(actualUri).isEqualTo(expectedUri);
    }

    @Test
    @DisplayName("выброс исключения для невалидной URI строки")
    void parse_invalidUriString_throwsException() {
        String urlString = "invalid-url-string";

        assertThatThrownBy(() -> linkParser.parse(urlString)).isInstanceOf(URISyntaxException.class);
    }
}
