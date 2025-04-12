package com.spaghetticodegang.trylater.recommendation.tag;

import com.spaghetticodegang.trylater.recommendation.tag.dto.TagGroupResponseDto;
import com.spaghetticodegang.trylater.recommendation.tag.dto.TagResponseDto;
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
    private final MessageUtil messageUtil;

    public Tag getTagById(Long tagId) {
        return tagRepository.findById(tagId)
                .orElseThrow(() -> new ValidationException(Map.of("tag", messageUtil.get("recommendation.tag.not.found"))));
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
