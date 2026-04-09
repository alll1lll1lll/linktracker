package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.repository.ChatRepository;
import org.springframework.jdbc.core.simple.JdbcClient;

public class JdbcChatRepository implements ChatRepository {
    private final JdbcClient jdbcClient;

    public JdbcChatRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void add(long chatId) {
        jdbcClient
                .sql("INSERT INTO chat (id) VALUES (:id) ON CONFLICT DO NOTHING")
                .param("id", chatId)
                .update();
    }

    @Override
    public void remove(long chatId) {
        jdbcClient.sql("DELETE FROM chat WHERE id = :id").param("id", chatId).update();
    }

    @Override
    public boolean exists(long chatId) {
        return Boolean.TRUE.equals(jdbcClient
                .sql("SELECT EXISTS(SELECT 1 FROM chat WHERE id = :id)")
                .param("id", chatId)
                .query(Boolean.class)
                .single());
    }
}
