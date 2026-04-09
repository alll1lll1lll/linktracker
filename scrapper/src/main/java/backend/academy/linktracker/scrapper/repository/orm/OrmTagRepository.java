package backend.academy.linktracker.scrapper.repository.orm;

import backend.academy.linktracker.scrapper.entity.TagEntity;
import backend.academy.linktracker.scrapper.model.TagModel;
import backend.academy.linktracker.scrapper.repository.TagRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrmTagRepository implements TagRepository {

    private final EntityManager entityManager;

    @Override
    public TagModel save(String name) {
        List<TagEntity> existing = entityManager
                .createQuery("SELECT t FROM TagEntity t WHERE t.name = :name", TagEntity.class)
                .setParameter("name", name)
                .getResultList();

        if (!existing.isEmpty()) {
            return toModel(existing.get(0));
        }
        TagEntity tag = new TagEntity();
        tag.setName(name);
        entityManager.persist(tag);
        return toModel(tag);
    }

    @Override
    public List<TagModel> findAll(int limit, int offset) {
        return entityManager
                .createQuery("SELECT t FROM TagEntity t ORDER BY t.id", TagEntity.class)
                .setMaxResults(limit)
                .setFirstResult(offset)
                .getResultStream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public void delete(long id) {
        TagEntity tag = entityManager.find(TagEntity.class, id);
        if (tag != null) {
            entityManager.remove(tag);
        }
    }

    @Override
    public TagModel update(long id, String newName) {
        TagEntity tag = entityManager.find(TagEntity.class, id);
        if (tag != null) {
            tag.setName(newName);
            return toModel(tag);
        }
        throw new IllegalArgumentException("тег не найден");
    }

    private TagModel toModel(TagEntity entity) {
        return new TagModel(entity.getId(), entity.getName());
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        entityManager
                .createQuery("DELETE FROM TagEntity t WHERE t.id IN (:ids)")
                .setParameter("ids", ids)
                .executeUpdate();
    }

    @Override
    public List<Long> findOrphanTagIds() {
        return entityManager.createQuery("""
                    SELECT t.id FROM TagEntity t
                    WHERE t.id NOT IN (SELECT st.id FROM SubscriptionEntity s JOIN s.tags st)
                    """, Long.class).getResultList();
    }
}
