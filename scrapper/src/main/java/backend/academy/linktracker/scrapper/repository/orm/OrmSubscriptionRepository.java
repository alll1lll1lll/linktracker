package backend.academy.linktracker.scrapper.repository.orm;

import backend.academy.linktracker.scrapper.dto.TagDto;
import backend.academy.linktracker.scrapper.entity.LinkEntity;
import backend.academy.linktracker.scrapper.entity.SubscriptionEntity;
import backend.academy.linktracker.scrapper.entity.TagEntity;
import backend.academy.linktracker.scrapper.model.LinkModel;
import backend.academy.linktracker.scrapper.model.SubscriptionId;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException; // Добавим для findOrCreateTag
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrmSubscriptionRepository implements SubscriptionRepository {

    private final EntityManager entityManager;

    @Override
    public void subscribe(long chatId, long linkId, List<String> tags) {
        SubscriptionId id = new SubscriptionId(chatId, linkId);
        SubscriptionEntity subscription = entityManager.find(SubscriptionEntity.class, id);

        if (subscription == null) {
            subscription = new SubscriptionEntity();
            subscription.setChatId(chatId);
            subscription.setLinkId(linkId);

            LinkEntity linkEntity = entityManager.find(LinkEntity.class, linkId);
            if (linkEntity == null) {
                throw new IllegalStateException("linkEntity с ID " + linkId + " не найдена. создайте ее сначала.");
            }
            subscription.setLink(linkEntity);

            entityManager.persist(subscription);
            entityManager.flush();
        }

        if (tags != null && !tags.isEmpty()) {
            for (String tagName : tags) {
                TagEntity tagEntity = findOrCreateTag(tagName);
                if (!subscription.getTags().contains(tagEntity)) {
                    subscription.getTags().add(tagEntity);
                }
            }
        }
    }

    @Override
    public void unsubscribe(long chatId, long linkId) {
        SubscriptionId id = new SubscriptionId(chatId, linkId);
        SubscriptionEntity subscription = entityManager.find(SubscriptionEntity.class, id);
        if (subscription != null) {
            subscription.getTags().clear();
            entityManager.remove(subscription);
            entityManager.flush();
        }
    }

    @Override
    public boolean isSubscribed(long chatId, long linkId) {
        return entityManager.find(SubscriptionEntity.class, new SubscriptionId(chatId, linkId)) != null;
    }

    @Override
    public List<LinkModel> findAllByChatId(long chatId) {
        return entityManager
                .createQuery("SELECT s.link FROM SubscriptionEntity s WHERE s.chatId = :chatId", LinkEntity.class)
                .setParameter("chatId", chatId)
                .getResultStream()
                .map(entity -> {
                    LinkModel m = new LinkModel();
                    m.setId(entity.getId());
                    m.setUrl(URI.create(entity.getUrl()));
                    m.setLastUpdated(entity.getLastUpdated());
                    return m;
                })
                .toList();
    }

    @Override
    public List<Long> findChatSubscribers(long linkId) {
        return entityManager
                .createQuery("SELECT s.chatId FROM SubscriptionEntity s WHERE s.link.id = :linkId", Long.class)
                .setParameter("linkId", linkId)
                .getResultList();
    }

    private TagEntity findOrCreateTag(String name) {
        try {
            return entityManager
                    .createQuery("SELECT t FROM TagEntity t WHERE t.name = :name", TagEntity.class)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException e) {
            TagEntity newTag = new TagEntity();
            newTag.setName(name);
            entityManager.persist(newTag);
            entityManager.flush();
            return newTag;
        }
    }

    @Override
    public Map<Long, List<String>> findAllTagsByChatId(long chatId) {
        List<TagDto> results = entityManager
                .createQuery("""
                    SELECT new backend.academy.linktracker.scrapper.dto.TagDto(s.link.id, t.name)
                    FROM SubscriptionEntity s
                    JOIN s.tags t
                    WHERE s.chatId = :chatId
                    """, TagDto.class)
                .setParameter("chatId", chatId)
                .getResultList();

        return results.stream()
                .collect(Collectors.groupingBy(
                        TagDto::linkId, Collectors.mapping(TagDto::tagName, Collectors.toList())));
    }
}
