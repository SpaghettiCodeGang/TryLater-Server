package com.spaghetticodegang.trylater.recommendation.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SeedTagGroupDto {

    private String tagGroupName;
    private List<String> tags;

}
