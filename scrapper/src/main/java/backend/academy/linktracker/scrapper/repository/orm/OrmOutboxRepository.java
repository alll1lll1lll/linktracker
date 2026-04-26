package backend.academy.linktracker.scrapper.repository.orm;

import backend.academy.linktracker.scrapper.entity.OutboxEntity;
import backend.academy.linktracker.scrapper.repository.OutboxRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrmOutboxRepository implements OutboxRepository {

    private final EntityManager entityManager;

    @Override
    public void save(OutboxEntity entity) {
        entityManager.persist(entity);
    }

    @Override
    public List<OutboxEntity> findPendingEvents(int limit) {
        return entityManager
                .createQuery("SELECT o FROM OutboxEntity o ORDER BY o.createdAt ASC", OutboxEntity.class)
                .setMaxResults(limit)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList();
    }

    @Override
    public void delete(Long id) {
        OutboxEntity entity = entityManager.find(OutboxEntity.class, id);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }
}
