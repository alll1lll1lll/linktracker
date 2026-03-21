package backend.academy.linktracker.scrapper.service;

import static org.slf4j.MDC.putCloseable;

import backend.academy.linktracker.scrapper.client.interfaces.LinkHandler;
import backend.academy.linktracker.scrapper.dto.request.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.request.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.dto.response.LinkResponse;
import backend.academy.linktracker.scrapper.dto.response.ListLinksResponse;
import backend.academy.linktracker.scrapper.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.exception.InvalidRequestException;
import backend.academy.linktracker.scrapper.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.exception.LinkNotFoundException;
import backend.academy.linktracker.scrapper.exception.LinkNotFoundForUserException;
import backend.academy.linktracker.scrapper.model.LinkModel;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinksService {
    private final LinkRepository linkRepository;
    private final ChatRepository chatRepository;
    private final List<LinkHandler> handlers;

    public LinkResponse add(long chatId, AddLinkRequest request) {
        URI url = request.getLink();
        try (var _ = putCloseable("chat_id", String.valueOf(chatId));
                var _ = putCloseable("url", String.valueOf(url))) {

            log.atInfo().addKeyValue("event", "link_add_attempt").log("action started");

            validateChatExists(chatId);
            validateHostSupported(url.getHost());

            LinkModel link = linkRepository.saveOrGet(url);
            if (link.getChatSubscribers().containsKey(chatId)) {
                log.atWarn().addKeyValue("event", "link_already_tracked").log("already tracked");
                throw new LinkAlreadyTrackedException(chatId, url.toString());
            }

            List<String> tags = request.getTags() != null ? request.getTags() : List.of();
            link.getChatSubscribers().put(chatId, tags);

            log.atInfo().addKeyValue("event", "link_add_success").log("action success");
            return new LinkResponse(link.getId(), link.getUrl(), tags);
        }
    }

    public LinkResponse remove(long chatId, RemoveLinkRequest request) {
        URI url = request.getLink();
        try (var _ = putCloseable("chat_id", String.valueOf(chatId));
                var _ = putCloseable("url", String.valueOf(url))) {

            log.atInfo().addKeyValue("event", "link_remove_attempt").log("action started");
            validateChatExists(chatId);

            LinkModel link =
                    linkRepository.findByUrl(url).orElseThrow(() -> new LinkNotFoundException("ссылка не найдена"));

            if (!link.getChatSubscribers().containsKey(chatId)) {
                log.atWarn().addKeyValue("event", "link_not_found_for_user").log("user not tracking");
                throw new LinkNotFoundForUserException("ссылка не найдена у пользователя");
            }

            List<String> removedTags = link.getChatSubscribers().remove(chatId);
            log.atInfo().addKeyValue("event", "link_remove_success").log("action success");

            return new LinkResponse(link.getId(), link.getUrl(), removedTags);
        }
    }

    public ListLinksResponse listAll(long chatId) {
        try (var _ = putCloseable("chat_id", String.valueOf(chatId))) {
            validateChatExists(chatId);

            List<LinkResponse> responses = linkRepository.findAllByChatId(chatId).stream()
                    .map(link -> new LinkResponse(
                            link.getId(),
                            link.getUrl(),
                            link.getChatSubscribers().get(chatId)))
                    .toList();

            log.atInfo()
                    .addKeyValue("event", "list_links_success")
                    .addKeyValue("count", responses.size())
                    .log("listed links");
            return new ListLinksResponse(responses, responses.size());
        }
    }

    private void validateChatExists(long chatId) {
        if (!chatRepository.exists(chatId)) {
            throw new ChatNotFoundException(chatId);
        }
    }

    private void validateHostSupported(String host) {
        if (handlers.stream().noneMatch(h -> h.getHost().equalsIgnoreCase(host))) {
            throw new InvalidRequestException("этот ресурс не поддерживается: " + host);
        }
    }
}
