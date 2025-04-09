package com.spaghetticodegang.trylater.recommendation.tag.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TagResponseDto {

    private Long id;
    private String tagName;

}

