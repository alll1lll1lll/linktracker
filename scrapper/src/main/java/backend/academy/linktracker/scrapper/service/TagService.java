package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.model.TagModel;
import backend.academy.linktracker.scrapper.repository.TagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;

    @Transactional
    public TagModel createTag(String name) {
        return tagRepository.save(name);
    }

    @Transactional(readOnly = true)
    public List<TagModel> getAllTags(int limit, int offset) {
        return tagRepository.findAll(limit, offset);
    }

    @Transactional
    public void deleteTag(long id) {
        tagRepository.delete(id);
    }

    @Transactional
    public TagModel renameTag(long id, String newName) {
        return tagRepository.update(id, newName);
    }
}
