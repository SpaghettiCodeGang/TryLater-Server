package com.spaghetticodegang.trylater.recommendation.dto;

import com.spaghetticodegang.trylater.recommendation.category.CategoryType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SeedCategoryDto {

    private CategoryType categoryType;
    private List<SeedTagGroupDto> tagGroups;

}
