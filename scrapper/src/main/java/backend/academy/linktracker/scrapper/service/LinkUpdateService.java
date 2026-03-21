package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.client.interfaces.LinkHandler;
import backend.academy.linktracker.scrapper.model.LinkModel;
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

    public LinkUpdateService(List<LinkHandler> handlers) {
        this.handlers = handlers.stream().collect(Collectors.toMap(LinkHandler::getHost, Function.identity()));
        log.atInfo()
                .addKeyValue("event", "handlers_initialized")
                .addKeyValue("count", handlers.size())
                .log("init handlers");
    }

    public void processUpdate(LinkModel link) {
        if (link == null || link.getUrl() == null || link.getUrl().getHost() == null) {
            log.atWarn().addKeyValue("event", "invalid_link_for_processing").log("not correct link");
            return;
        }
        try (var _ = MDC.putCloseable("link_id", String.valueOf(link.getId()));
                var _ = MDC.putCloseable("link_url", link.getUrl().toString())) {

            String host = link.getUrl().getHost().toLowerCase();
            LinkHandler handler = handlers.get(host);

            if (handler == null) {
                log.atWarn().addKeyValue("event", "no_handler_found").log("handler with this host was not found");
                return;
            }

            try {
                handler.handle(link);
            } catch (Exception e) {
                log.atError()
                        .addKeyValue("event", "link_processing_error")
                        .addKeyValue("handler", handler.getClass().getSimpleName())
                        .setCause(e)
                        .log("error handle link");
            }
        }
    }
}
