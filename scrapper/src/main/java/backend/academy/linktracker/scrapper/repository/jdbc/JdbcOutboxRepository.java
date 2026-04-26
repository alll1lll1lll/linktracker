package backend.academy.linktracker.scrapper.repository.jdbc;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcOutboxRepository {
    private final JdbcClient jdbcClient;

    public JdbcOutboxRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public void save(long linkId, String url, String description, List<Long> chatIds) {
        jdbcClient
                .sql(
                        "INSERT INTO outbox_event (link_id, url, description, chat_ids) VALUES (:linkId, :url, :desc, :chatIds)")
                .param("linkId", linkId)
                .param("url", url)
                .param("desc", description)
                .param("chatIds", chatIds.toArray(new Long[0]))
                .update();
    }

    public List<Map<String, Object>> findTop100() {
        return jdbcClient
                .sql("SELECT * FROM outbox_event ORDER BY created_at ASC LIMIT 100 FOR UPDATE SKIP LOCKED")
                .query()
                .listOfRows();
    }

    public void delete(long id) {
        jdbcClient
                .sql("DELETE FROM outbox_event WHERE id = :id")
                .param("id", id)
                .update();
    }
}
