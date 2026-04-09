package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.model.LinkModel;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import java.net.URI;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.simple.JdbcClient;

public class JdbcSubscriptionRepository implements SubscriptionRepository {
    private final JdbcClient jdbcClient;

    public JdbcSubscriptionRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void subscribe(long chatId, long linkId, List<String> tags) {
        jdbcClient
                .sql("INSERT INTO subscription (chat_id, link_id) VALUES (:chatId, :linkId) ON CONFLICT DO NOTHING")
                .param("chatId", chatId)
                .param("linkId", linkId)
                .update();

        if (tags != null && !tags.isEmpty()) {
            jdbcClient
                    .sql("""
                WITH inserted_tags AS (
                    INSERT INTO tag (name)
                    SELECT unnest(:tagNames)
                    ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name
                    RETURNING id
                )
                INSERT INTO subscription_tag (chat_id, link_id, tag_id)
                SELECT :chatId, :linkId, id FROM inserted_tags
                ON CONFLICT DO NOTHING
            """)
                    .param("chatId", chatId)
                    .param("linkId", linkId)
                    .param("tagNames", tags.toArray(new String[0]))
                    .update();
        }
    }

    @Override
    public void unsubscribe(long chatId, long linkId) {
        jdbcClient
                .sql("DELETE FROM subscription WHERE chat_id = :chatId AND link_id = :linkId")
                .param("chatId", chatId)
                .param("linkId", linkId)
                .update();
    }

    @Override
    public boolean isSubscribed(long chatId, long linkId) {
        return jdbcClient
                .sql("SELECT 1 FROM subscription WHERE chat_id = :chatId AND link_id = :linkId")
                .param("chatId", chatId)
                .param("linkId", linkId)
                .query(Integer.class)
                .optional()
                .isPresent();
    }

    @Override
    @SuppressWarnings("PMD.UnusedFormalParameter")
    public List<LinkModel> findAllByChatId(long chatId) {
        return jdbcClient
                .sql("""
                    SELECT l.id, l.url, l.last_updated
                    FROM link l
                    JOIN subscription s ON l.id = s.link_id
                    WHERE s.chat_id = :chatId
                """)
                .param("chatId", chatId)
                .query((rs, rowNum) -> {
                    LinkModel model = new LinkModel();
                    model.setId(rs.getLong("id"));
                    model.setUrl(URI.create(rs.getString("url")));
                    model.setLastUpdated(rs.getObject("last_updated", java.time.OffsetDateTime.class));
                    return model;
                })
                .list();
    }

    @Override
    public List<Long> findChatSubscribers(long linkId) {
        return jdbcClient
                .sql("SELECT chat_id FROM subscription WHERE link_id = :linkId")
                .param("linkId", linkId)
                .query(Long.class)
                .list();
    }

    @Override
    @SuppressWarnings("PMD.UnusedFormalParameter")
    public Map<Long, List<String>> findAllTagsByChatId(long chatId) {
        return jdbcClient
                .sql("""
                    SELECT st.link_id, t.name
                    FROM tag t
                    JOIN subscription_tag st ON t.id = st.tag_id
                    WHERE st.chat_id = :chatId
                """)
                .param("chatId", chatId)
                .query((rs, rowNum) -> new AbstractMap.SimpleEntry<>(rs.getLong("link_id"), rs.getString("name")))
                .list()
                .stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }
}
