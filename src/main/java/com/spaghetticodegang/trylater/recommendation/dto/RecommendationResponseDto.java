package com.spaghetticodegang.trylater.recommendation.dto;

import com.spaghetticodegang.trylater.recommendation.category.CategoryType;
import com.spaghetticodegang.trylater.recommendation.tag.dto.TagGroupResponseDto;
import com.spaghetticodegang.trylater.user.dto.UserResponseDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class RecommendationResponseDto {

    private Long id;
    private String title;
    private String description;
    private String imgPath;
    private URL url;
    private int rating;
    private UserResponseDto creator;
    private LocalDateTime creationDate;
    private CategoryType category;
    private List<TagGroupResponseDto> tagGroups;

}
