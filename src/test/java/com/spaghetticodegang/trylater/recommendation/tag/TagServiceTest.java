package com.spaghetticodegang.trylater.recommendation.tag;

import com.spaghetticodegang.trylater.recommendation.category.Category;
import com.spaghetticodegang.trylater.recommendation.category.CategoryRepository;
import com.spaghetticodegang.trylater.recommendation.category.CategoryType;
import com.spaghetticodegang.trylater.recommendation.tag.dto.TagGroupResponseDto;
import com.spaghetticodegang.trylater.recommendation.tag.dto.TagResponseDto;
import com.spaghetticodegang.trylater.recommendation.tag.group.TagGroup;
import com.spaghetticodegang.trylater.recommendation.tag.group.TagGroupRepository;
import com.spaghetticodegang.trylater.shared.exception.ValidationException;
import com.spaghetticodegang.trylater.shared.util.MessageUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagGroupRepository tagGroupRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MessageUtil messageUtil;

    @InjectMocks
    private TagService tagService;

    private Tag createTag(Long id, String tagName, String groupName) {
        return Tag.builder()
                .id(id)
                .tagName(tagName)
                .tagGroup(TagGroup.builder()
                        .tagGroupName(groupName)
                        .build())
                .build();
    }

    @Test
    void shouldReturnTag_whenTagExists() {
        Tag tag = createTag(1L, "Action", "Genre");

        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        Tag result = tagService.getTagById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Action", result.getTagName());
        assertEquals("Genre", result.getTagGroup().getTagGroupName());
    }

    @Test
    void shouldThrowValidationException_whenTagNotFound() {
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());
        when(messageUtil.get("recommendation.tag.not.found")).thenReturn("Tag wurde nicht gefunden");

        ValidationException ex = assertThrows(ValidationException.class, () -> {
            tagService.getTagById(1L);
        });

        assertTrue(ex.getErrors().containsKey("tag"));
        assertEquals("Tag wurde nicht gefunden", ex.getErrors().get("tag"));
    }

    @Test
    void shouldGroupTagsCorrectlyByTagGroupName() {
        Tag tag1 = createTag(1L, "Action", "Genre");
        Tag tag2 = createTag(2L, "Komödie", "Genre");
        Tag tag3 = createTag(3L, "Mittel", "Länge");

        List<Tag> tags = List.of(tag1, tag2, tag3);

        List<TagGroupResponseDto> result = tagService.createTagGroupResponseDtoFromTags(tags);

        assertEquals(2, result.size());

        TagGroupResponseDto genreGroup = result.stream()
                .filter(group -> group.getTagGroupName().equals("Genre"))
                .findFirst()
                .orElseThrow();

        assertEquals(2, genreGroup.getTags().size());
        assertTrue(genreGroup.getTags().stream().map(TagResponseDto::getTagName).toList().containsAll(List.of("Action", "Komödie")));

        TagGroupResponseDto epocheGroup = result.stream()
                .filter(group -> group.getTagGroupName().equals("Länge"))
                .findFirst()
                .orElseThrow();

        assertEquals(1, epocheGroup.getTags().size());
        assertEquals("Mittel", epocheGroup.getTags().getFirst().getTagName());
    }

    @Test
    void shouldReturnTagGroupsForValidCategoryType() {
        // Arrange
        CategoryType categoryType = CategoryType.MEDIA;
        Category category = Category.builder().categoryType(categoryType).build();

        Tag tag1 = createTag(1L, "Action", "Genre");
        Tag tag2 = createTag(2L, "Komödie", "Genre");

        TagGroup tagGroup = TagGroup.builder()
                .tagGroupName("Genre")
                .tags(List.of(tag1, tag2))
                .build();

        tag1.setTagGroup(tagGroup);
        tag2.setTagGroup(tagGroup);

        when(categoryRepository.findByCategoryType(categoryType)).thenReturn(Optional.of(category));
        when(tagGroupRepository.findAllByCategory(category)).thenReturn(List.of(tagGroup));

        List<TagGroupResponseDto> result = tagService.getTagsByCategory(categoryType);

        assertEquals(1, result.size());
        TagGroupResponseDto group = result.getFirst();
        assertEquals("Genre", group.getTagGroupName());
        assertEquals(2, group.getTags().size());
        assertTrue(group.getTags().stream().map(TagResponseDto::getTagName).toList().containsAll(List.of("Action", "Komödie")));
    }

}
