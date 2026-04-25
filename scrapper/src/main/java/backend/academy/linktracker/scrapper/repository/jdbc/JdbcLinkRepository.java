package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.model.LinkModel;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;

public class JdbcLinkRepository implements LinkRepository {
    private final JdbcClient jdbcClient;

    public JdbcLinkRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public LinkModel saveOrGet(URI url) {
        return jdbcClient
                .sql("""
                    INSERT INTO link (url) VALUES (:url)
                    ON CONFLICT (url) DO UPDATE SET url = EXCLUDED.url
                    RETURNING id, url, last_updated, last_checked_at
                """)
                .param("url", url.toString())
                .query(this::mapRow)
                .single();
    }

    @Override
    public Optional<LinkModel> findByUrl(URI url) {
        return jdbcClient
                .sql("SELECT id, url, last_updated, last_checked_at FROM link WHERE url = :url")
                .param("url", url.toString())
                .query(this::mapRow)
                .optional();
    }

    @Override
    public List<LinkModel> findLinksToUpdate(int limit, OffsetDateTime now) {
        return jdbcClient
                .sql("""
                    UPDATE link
                    SET last_checked_at = :now
                    WHERE id IN (
                        SELECT id
                        FROM link
                        WHERE last_checked_at IS NULL
                           OR last_checked_at < (:now - INTERVAL '1 minute')
                        ORDER BY last_checked_at ASC NULLS FIRST
                        LIMIT :limit
                        FOR UPDATE SKIP LOCKED
                    )
                    RETURNING id, url, last_updated, last_checked_at
                """)
                .param("limit", limit)
                .param("now", now)
                .query(this::mapRow)
                .list();
    }

    @Override
    public void updateLastUpdated(long linkId, OffsetDateTime time) {
        jdbcClient
                .sql("UPDATE link SET last_updated = :time WHERE id = :id")
                .param("id", linkId)
                .param("time", time)
                .update();
    }

    @SuppressWarnings("PMD.UnusedFormalParameter")
    private LinkModel mapRow(ResultSet rs, int rowNum) throws SQLException {
        LinkModel model = new LinkModel();
        model.setId(rs.getLong("id"));
        model.setUrl(URI.create(rs.getString("url")));
        model.setLastUpdated(rs.getObject("last_updated", OffsetDateTime.class));
        model.setLastCheckedAt(rs.getObject("last_checked_at", OffsetDateTime.class));
        return model;
    }
}
