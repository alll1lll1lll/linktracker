package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.model.TagModel;
import backend.academy.linktracker.scrapper.repository.TagRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;

public class JdbcTagRepository implements TagRepository {
    private final JdbcClient jdbcClient;

    public JdbcTagRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public TagModel save(String name) {
        return jdbcClient.sql("""
                INSERT INTO tag (name) VALUES (:name)
                ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name
                RETURNING id, name
            """).param("name", name).query(this::mapRow).single();
    }

    @Override
    public List<TagModel> findAll(int limit, int offset) {
        return jdbcClient
                .sql("SELECT id, name FROM tag ORDER BY id LIMIT :limit OFFSET :offset")
                .param("limit", limit)
                .param("offset", offset)
                .query(this::mapRow)
                .list();
    }

    @Override
    public void delete(long id) {
        jdbcClient.sql("DELETE FROM tag WHERE id = :id").param("id", id).update();
    }

    @Override
    @SuppressWarnings("PMD.UnusedFormalParameter")
    public TagModel update(long id, String newName) {
        return jdbcClient
                .sql("UPDATE tag SET name = :name WHERE id = :id RETURNING id, name")
                .param("id", id)
                .param("name", newName)
                .query(this::mapRow)
                .single();
    }

    @Override
    public List<Long> findOrphanTagIds() {
        return jdbcClient
                .sql("SELECT id FROM tag WHERE id NOT IN (SELECT tag_id FROM subscription_tag)")
                .query(Long.class)
                .list();
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        jdbcClient.sql("DELETE FROM tag WHERE id IN (:ids)").param("ids", ids).update();
    }

    @SuppressWarnings("PMD.UnusedFormalParameter")
    private TagModel mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new TagModel(rs.getLong("id"), rs.getString("name"));
    }
}
