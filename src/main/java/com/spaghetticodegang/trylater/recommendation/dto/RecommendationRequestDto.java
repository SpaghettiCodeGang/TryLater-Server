package com.spaghetticodegang.trylater.recommendation.dto;

import com.spaghetticodegang.trylater.recommendation.category.CategoryType;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class RecommendationRequestDto {

    @NotBlank(message = "{recommendation.title.notblank}")
    private String title;

    @Size(max = 2000, message = "{recommendation.description.max.value}")
    private String description;

    private String imgPath;

    @URL(message = "{recommendation.url.invalid}")
    private String url;

    @NotNull(message = "{recommendation.rating.notblank}")
    @Min(value = 1, message = "{recommendation.rating.min.value}")
    @Max(value = 3, message = "{recommendation.rating.max.value}")
    private int rating;

    @NotNull(message = "{recommendation.category.not.null}")
    private CategoryType category;

    @NotEmpty(message = "{recommendation.receiver.not.empty}")
    private List<Long> receiverIds;

    private List<Long> tagIds;
}
