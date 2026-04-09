package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.model.LinkModel;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface LinkRepository {
    LinkModel saveOrGet(URI url);

    Optional<LinkModel> findByUrl(URI url);

    List<LinkModel> findLinksToUpdate(int limit, int offset);

    void updateLastUpdated(long linkId, OffsetDateTime time);
}
