package com.spaghetticodegang.trylater.recommendation.tag.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class TagGroupResponseDto {

    private String tagGroupName;
    private List<TagResponseDto> tags;

}
