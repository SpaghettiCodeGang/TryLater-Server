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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final TagGroupRepository tagGroupRepository;
    private final CategoryRepository categoryRepository;
    private final MessageUtil messageUtil;

    /**
     * Retrieves a {@link Tag} entity by its ID.
     *
     * @param tagId the ID of the tag to retrieve
     * @return the corresponding {@link Tag} entity
     * @throws ValidationException if no tag is found for the given ID
     */
    public Tag getTagById(Long tagId) {
        return tagRepository.findById(tagId)
                .orElseThrow(() -> new ValidationException(Map.of("tag", messageUtil.get("recommendation.tag.not.found"))));
    }

    /**
     * Retrieves all tag groups including their tags for a given category type.
     *
     * @param categoryType the category type used for filtering
     * @return a list of {@link TagGroupResponseDto} representing grouped tags
     * @throws IllegalArgumentException if the category type is not found in the database
     */
    public List<TagGroupResponseDto> getTagsByCategory(CategoryType categoryType) {

        Category category = categoryRepository.findByCategoryType(categoryType)
                .orElseThrow(() -> new IllegalArgumentException("Kategorie nicht gefunden: " + categoryType));

        List<TagGroup> tagGroups = tagGroupRepository.findAllByCategory(category);

        return tagGroups.stream()
                .map(tagGroup -> TagGroupResponseDto.builder()
                        .tagGroupId(tagGroup.getId())
                        .tagGroupName(tagGroup.getTagGroupName())
                        .tags(tagGroup.getTags().stream()
                                .map(tag -> TagResponseDto.builder()
                                        .id(tag.getId())
                                        .tagName(tag.getTagName())
                                        .build()).toList())
                        .build())
                .toList();
    }

    /**
     * Creates a list of {@link TagGroupResponseDto} objects grouped by tag group name
     * from a list of {@link Tag} entities.
     *
     * @param tags the tag entities
     * @return a list of response DTOs representing the tag groups including tags
     */
    public List<TagGroupResponseDto> createTagGroupResponseDtoFromTags(List<Tag> tags) {
        Map<String, List<TagResponseDto>> groupedTags = tags.stream()
                .collect(Collectors.groupingBy(
                        tag -> tag.getTagGroup().getTagGroupName(),
                        Collectors.mapping(
                                tag -> TagResponseDto.builder()
                                        .id(tag.getId())
                                        .tagName(tag.getTagName())
                                        .build(),
                                Collectors.toList()
                        )
                ));

        return groupedTags.entrySet().stream()
                .map(entry -> TagGroupResponseDto.builder()
                        .tagGroupName(entry.getKey())
                        .tags(entry.getValue())
                        .build())
                .toList();
    }
}
