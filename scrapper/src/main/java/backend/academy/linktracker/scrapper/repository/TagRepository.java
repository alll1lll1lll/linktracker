package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.model.TagModel;
import java.util.List;

public interface TagRepository {
    TagModel save(String name);

    List<TagModel> findAll(int limit, int offset);

    void delete(long id);

    TagModel update(long id, String newName);

    List<Long> findOrphanTagIds();

    void deleteByIds(List<Long> ids);
}
