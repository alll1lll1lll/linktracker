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
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import backend.academy.linktracker.scrapper.repository.TagRepository;
import java.net.URI;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinksService {

    private final LinkRepository linkRepository;
    private final ChatRepository chatRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final TagRepository tagRepository;
    private final List<LinkHandler> handlers;

    @Transactional
    public LinkResponse add(long chatId, AddLinkRequest request) {
        URI url = request.getLink();
        try (var _ = putCloseable("chat_id", String.valueOf(chatId));
                var _ = putCloseable("url", String.valueOf(url))) {

            log.atInfo().addKeyValue("event", "link_add_attempt").log("action started");

            validateChatExists(chatId);
            validateHostSupported(url.getHost());

            LinkModel link = linkRepository.saveOrGet(url);

            if (subscriptionRepository.isSubscribed(chatId, link.getId())) {
                log.atWarn().addKeyValue("event", "link_already_tracked").log("already tracked");
                throw new LinkAlreadyTrackedException(chatId, url.toString());
            }

            List<String> tags = request.getTags() != null ? request.getTags() : List.of();
            subscriptionRepository.subscribe(chatId, link.getId(), tags);

            log.atInfo().addKeyValue("event", "link_add_success").log("action success");
            return new LinkResponse(link.getId(), link.getUrl(), tags);
        }
    }

    @Transactional
    public LinkResponse remove(long chatId, RemoveLinkRequest request) {
        URI url = request.getLink();
        try (var _ = putCloseable("chat_id", String.valueOf(chatId));
                var _ = putCloseable("url", String.valueOf(url))) {

            log.atInfo().addKeyValue("event", "link_remove_attempt").log("action started");

            validateChatExists(chatId);

            LinkModel link =
                    linkRepository.findByUrl(url).orElseThrow(() -> new LinkNotFoundException("ссылка не найдена"));

            if (!subscriptionRepository.isSubscribed(chatId, link.getId())) {
                log.atWarn().addKeyValue("event", "link_not_found_for_user").log("user not tracking");
                throw new LinkNotFoundForUserException("ссылка не найдена у пользователя");
            }
            subscriptionRepository.unsubscribe(chatId, link.getId());
            List<Long> orphanTagIds = tagRepository.findOrphanTagIds();
            if (!orphanTagIds.isEmpty()) {
                tagRepository.deleteByIds(orphanTagIds);
                log.atInfo()
                        .addKeyValue("event", "tags_cleaned_up")
                        .addKeyValue("count", orphanTagIds.size())
                        .log("orphaned tags removed");
            }
            log.atInfo().addKeyValue("event", "link_remove_success").log("action success");
            return new LinkResponse(link.getId(), link.getUrl(), List.of());
        }
    }

    @Transactional(readOnly = true)
    public ListLinksResponse listAll(long chatId) {
        try (var _ = putCloseable("chat_id", String.valueOf(chatId))) {
            validateChatExists(chatId);
            List<LinkModel> links = subscriptionRepository.findAllByChatId(chatId);
            Map<Long, List<String>> tagsByLinkId = subscriptionRepository.findAllTagsByChatId(chatId);
            List<LinkResponse> responses = links.stream()
                    .map(link -> {
                        List<String> tags = tagsByLinkId.getOrDefault(link.getId(), List.of());
                        return new LinkResponse(link.getId(), link.getUrl(), tags);
                    })
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
