package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.model.LinkModel;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class LinkRepository {
    private final Map<URI, LinkModel> links = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public LinkModel saveOrGet(URI url) {
        return links.computeIfAbsent(url, k -> {
            LinkModel model = new LinkModel();
            model.setId(idGenerator.getAndIncrement());
            model.setUrl(url);
            return model;
        });
    }

    public List<LinkModel> findAllByChatId(long chatId) {
        return links.values().stream()
                .filter(link -> link.getChatSubscribers().containsKey(chatId))
                .toList();
    }

    public Optional<LinkModel> findByUrl(URI url) {
        return Optional.ofNullable(links.get(url));
    }

    public List<LinkModel> findAll() {
        return new ArrayList<>(links.values());
    }
}
