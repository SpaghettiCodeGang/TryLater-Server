package com.spaghetticodegang.trylater.recommendation.assignment.dto;

import com.spaghetticodegang.trylater.recommendation.assignment.RecommendationAssignmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecommendationAssignmentStatusRequestDto {

    @NotNull
    RecommendationAssignmentStatus recommendationAssignmentStatus;
}