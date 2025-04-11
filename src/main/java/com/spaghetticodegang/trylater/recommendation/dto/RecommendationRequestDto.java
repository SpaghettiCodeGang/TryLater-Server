package com.spaghetticodegang.trylater.recommendation.dto;

import com.spaghetticodegang.trylater.recommendation.category.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.net.URL;
import java.util.List;

@Getter
@Setter
@Builder
public class RecommendationRequestDto {

    @NotBlank
    private String title;

    private String description;
    private String imgPath;
    private URL url;

    @NotNull
    private int rating;

    @NotNull
    private CategoryType category;

    @NotEmpty
    private List<Long> receiverIds;

    @NotEmpty
    private List<Long> tagIds;
}
