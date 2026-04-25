package backend.academy.linktracker.scrapper.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.client.interfaces.LinkHandler;
import backend.academy.linktracker.scrapper.model.LinkModel;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LinkUpdateServiceTest {

    @Test
    @DisplayName("сервис корректно обрабатывает недоступность внешних API и обновляет время")
    void processUpdate() {
        LinkRepository linkRepository = mock(LinkRepository.class);

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
        LinkUpdateService updateService = new LinkUpdateService(List.of(handler), linkRepository);
        LinkModel model = new LinkModel();
        model.setId(1L);
        model.setUrl(URI.create("https://github.com/test/repo"));

        assertDoesNotThrow(() -> updateService.processUpdate(model));
        verify(linkRepository, times(1)).updateLastUpdated(eq(1L), any());
    }

    @Test
    @DisplayName("сервис успешно делегирует обработку подходящему хэндлеру и обновляет время")
    void processUpdate_Success() {
        LinkRepository linkRepository = mock(LinkRepository.class);

        LinkModel model = new LinkModel();
        model.setId(1L);
        model.setUrl(URI.create("https://github.com/test/repo"));

        LinkHandler handler = mock(LinkHandler.class);
        when(handler.getHost()).thenReturn("github.com");

        LinkUpdateService updateService = new LinkUpdateService(List.of(handler), linkRepository);

        updateService.processUpdate(model);
        verify(handler, times(1)).handle(model);
        verify(linkRepository, times(1)).updateLastUpdated(eq(1L), any());
    }
}
