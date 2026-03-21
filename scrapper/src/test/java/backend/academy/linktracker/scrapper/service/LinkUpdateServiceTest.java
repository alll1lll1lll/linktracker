package backend.academy.linktracker.scrapper.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.client.interfaces.LinkHandler;
import backend.academy.linktracker.scrapper.model.LinkModel;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LinkUpdateServiceTest {

    @Test
    @DisplayName("сервис корректно обрабатывает недоступность внешних API")
    void processUpdate() {
        LinkHandler handler = new LinkHandler() {
            @Override
            public String getHost() {
                return "github.com";
            }

            @Override
            public void handle(LinkModel linkModel) {
                throw new RuntimeException("500 internal Server Error from GitHub API");
            }
        };

        LinkUpdateService updateService = new LinkUpdateService(List.of(handler));

        LinkModel model = new LinkModel();
        model.setId(1L);
        model.setUrl(URI.create("https://github.com/test/repo"));

        assertDoesNotThrow(() -> updateService.processUpdate(model));
    }

    @Test
    @DisplayName("сервис успешно делегирует обработку подходящему хэндлеру")
    void processUpdate_Success() {
        LinkModel model = new LinkModel();
        model.setUrl(URI.create("https://github.com/test/repo"));

        LinkHandler handler = mock(LinkHandler.class);
        when(handler.getHost()).thenReturn("github.com");

        LinkUpdateService updateService = new LinkUpdateService(List.of(handler));

        updateService.processUpdate(model);

        verify(handler, times(1)).handle(model);
    }
}
