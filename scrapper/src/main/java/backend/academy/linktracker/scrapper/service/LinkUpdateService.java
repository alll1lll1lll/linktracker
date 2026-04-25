package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.client.interfaces.LinkHandler;
import backend.academy.linktracker.scrapper.model.LinkModel;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LinkUpdateService {

    private final Map<String, LinkHandler> handlers;

    private final LinkRepository linkRepository;

    public LinkUpdateService(List<LinkHandler> handlers, LinkRepository linkRepository) {
        this.handlers = handlers.stream().collect(Collectors.toMap(LinkHandler::getHost, Function.identity()));
        this.linkRepository = linkRepository;
        log.atInfo()
                .addKeyValue("event", "handlers_initialized")
                .addKeyValue("count", handlers.size())
                .log("init handlers");
    }

    public boolean processUpdate(LinkModel link) {
        if (link == null || link.getUrl() == null || link.getUrl().getHost() == null) {
            log.atWarn().addKeyValue("event", "invalid_link_for_processing").log("not correct link");
            return false;
        }

        try (var _ = MDC.putCloseable("link_id", String.valueOf(link.getId()));
                var _ = MDC.putCloseable("link_url", link.getUrl().toString())) {

            String host = link.getUrl().getHost().toLowerCase();
            LinkHandler handler = handlers.get(host);

            if (handler == null) {
                log.atWarn().addKeyValue("event", "no_handler_found").log("handler not found");
                linkRepository.updateLastUpdated(link.getId(), OffsetDateTime.now());
                return false;
            }
            try {
                handler.handle(link);
                return true;
            } catch (Exception e) {
                log.atError()
                        .addKeyValue("event", "link_processing_error")
                        .setCause(e)
                        .log("error handle link");
                return false;
            } finally {
                linkRepository.updateLastUpdated(link.getId(), OffsetDateTime.now());
            }
        }
    }
}
