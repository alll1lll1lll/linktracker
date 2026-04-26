package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.entity.OutboxEntity;
import java.util.List;

public interface OutboxRepository {
    void save(OutboxEntity entity);

    List<OutboxEntity> findPendingEvents(int limit);

    void delete(Long id);
}
