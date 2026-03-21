package backend.academy.linktracker.scrapper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.client.interfaces.LinkHandler;
import backend.academy.linktracker.scrapper.dto.request.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.request.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.dto.response.LinkResponse;
import backend.academy.linktracker.scrapper.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.exception.LinkNotFoundForUserException;
import backend.academy.linktracker.scrapper.model.LinkModel;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class LinkServiceTest {

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private LinkHandler linkHandler;

    @InjectMocks
    private LinksService linksService;

    long chatId = 1L;

    @Test
    void add_ShouldThrowException_WhenChatDoesNotExist() {
        AddLinkRequest request = new AddLinkRequest(URI.create("https://github.com/test"), List.of());
        when(chatRepository.exists(chatId)).thenReturn(false);

        assertThatThrownBy(() -> linksService.add(chatId, request)).isInstanceOf(ChatNotFoundException.class);
    }

    @Test
    void add_ShouldSuccessfullyAddLink_WhenDataIsValid() {
        URI url = URI.create("https://github.com/test");
        AddLinkRequest request = new AddLinkRequest(url, List.of("java"));

        LinkModel linkModel = new LinkModel();
        linkModel.setChatSubscribers(new HashMap<>());

        when(chatRepository.exists(chatId)).thenReturn(true);
        when(linkHandler.getHost()).thenReturn("github.com");
        ReflectionTestUtils.setField(linksService, "handlers", List.of(linkHandler));
        when(linkRepository.saveOrGet(url)).thenReturn(linkModel);

        LinkResponse response = linksService.add(chatId, request);

        assertThat(response).isNotNull();
        assertThat(linkModel.getChatSubscribers()).containsKey(chatId);
        assertThat(linkModel.getChatSubscribers().get(chatId)).contains("java");
        verify(linkRepository, times(1)).saveOrGet(url);
    }

    @Test
    void remove_ShouldThrowException_WhenLinkNotFoundForUser() {
        URI url = URI.create("https://github.com/test");
        RemoveLinkRequest request = new RemoveLinkRequest(url);

        LinkModel linkModel = new LinkModel();
        linkModel.setChatSubscribers(new ConcurrentHashMap<>());

        when(chatRepository.exists(chatId)).thenReturn(true);
        when(linkRepository.findByUrl(url)).thenReturn(Optional.of(linkModel));

        assertThatThrownBy(() -> linksService.remove(chatId, request)).isInstanceOf(LinkNotFoundForUserException.class);
    }
}
