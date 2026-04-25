package backend.academy.linktracker.scrapper.repository.orm;

import backend.academy.linktracker.scrapper.entity.LinkEntity;
import backend.academy.linktracker.scrapper.model.LinkModel;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class OrmLinkRepository implements LinkRepository {

    private final EntityManager entityManager;

    @Override
    public LinkModel saveOrGet(URI url) {
        LinkEntity entity = (LinkEntity) entityManager
                .createNativeQuery("""
            INSERT INTO link (url) VALUES (:url)
            ON CONFLICT (url) DO UPDATE SET url = EXCLUDED.url
            RETURNING *
            """, LinkEntity.class)
                .setParameter("url", url.toString())
                .getSingleResult();

        return toModel(entity);
    }

    @Override
    public Optional<LinkModel> findByUrl(URI url) {
        return entityManager
                .createQuery("SELECT l FROM LinkEntity l WHERE l.url = :url", LinkEntity.class)
                .setParameter("url", url.toString())
                .getResultStream()
                .findFirst()
                .map(this::toModel);
    }

    @Override
    @Transactional
    public List<LinkModel> findLinksToUpdate(int limit, OffsetDateTime now) {
        return entityManager
                .createQuery("SELECT l FROM LinkEntity l ORDER BY l.lastUpdated ASC NULLS FIRST", LinkEntity.class)
                .setMaxResults(limit)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultStream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public void updateLastUpdated(long linkId, OffsetDateTime time) {
        LinkEntity link = entityManager.find(LinkEntity.class, linkId);
        if (link != null) {
            link.setLastUpdated(time);
        }
    }

    private LinkModel toModel(LinkEntity entity) {
        LinkModel model = new LinkModel();
        model.setId(entity.getId());
        model.setUrl(URI.create(entity.getUrl()));
        model.setLastUpdated(entity.getLastUpdated());
        model.setLastCheckedAt(entity.getLastCheckedAt());
        return model;
    }
}
