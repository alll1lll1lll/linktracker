package backend.academy.linktracker.scrapper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.model.TagModel;
import backend.academy.linktracker.scrapper.repository.TagRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    @Test
    @DisplayName("должен успешно создавать тег")
    void createTag_ShouldReturnCreatedTag() {
        String tagName = "jsd";
        TagModel expectedTag = new TagModel(1L, tagName);
        when(tagRepository.save(tagName)).thenReturn(expectedTag);

        TagModel actualTag = tagService.createTag(tagName);

        assertThat(actualTag).isNotNull();
        assertThat(actualTag.getName()).isEqualTo(tagName);
        assertThat(actualTag.getId()).isEqualTo(1L);
        verify(tagRepository, times(1)).save(tagName);
    }

    @Test
    @DisplayName("должен возвращать список тегов с учетом лимита и смещения")
    void getAllTags_ShouldReturnListOfTags() {
        int limit = 10;
        int offset = 0;
        List<TagModel> expectedTags = List.of(new TagModel(1L, "lll"), new TagModel(2L, "dddd"));
        when(tagRepository.findAll(limit, offset)).thenReturn(expectedTags);

        List<TagModel> actualTags = tagService.getAllTags(limit, offset);

        assertThat(actualTags).hasSize(2);
        assertThat(actualTags).containsExactlyInAnyOrderElementsOf(expectedTags);
        verify(tagRepository, times(1)).findAll(limit, offset);
    }

    @Test
    @DisplayName("должен вызывать метод удаления в репозитории")
    void deleteTag_ShouldInvokeRepositoryDelete() {
        long tagId = 1L;

        tagService.deleteTag(tagId);

        verify(tagRepository, times(1)).delete(tagId);
    }

    @Test
    @DisplayName("должен успешно переименовывать тег")
    void renameTag_ShouldReturnUpdatedTag() {
        long tagId = 1L;
        String newName = "danilkolbasenko";
        TagModel updatedTag = new TagModel(tagId, newName);
        when(tagRepository.update(tagId, newName)).thenReturn(updatedTag);

        TagModel result = tagService.renameTag(tagId, newName);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(newName);
        assertThat(result.getId()).isEqualTo(tagId);
        verify(tagRepository, times(1)).update(tagId, newName);
    }
}
