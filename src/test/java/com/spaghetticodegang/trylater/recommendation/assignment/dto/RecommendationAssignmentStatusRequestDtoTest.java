package com.spaghetticodegang.trylater.recommendation.assignment.dto;

import com.spaghetticodegang.trylater.recommendation.assignment.RecommendationAssignmentStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecommendationAssignmentStatusRequestDtoTest {

    @Test
    void shouldSetAndGetRecommendationAssignmentStatus() {
        RecommendationAssignmentStatusRequestDto dto = new RecommendationAssignmentStatusRequestDto();
        dto.setRecommendationAssignmentStatus(RecommendationAssignmentStatus.ACCEPTED);
        assertEquals(RecommendationAssignmentStatus.ACCEPTED, dto.getRecommendationAssignmentStatus());
    }
}
