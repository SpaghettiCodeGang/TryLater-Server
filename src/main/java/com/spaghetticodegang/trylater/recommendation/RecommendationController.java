package com.spaghetticodegang.trylater.recommendation;

import com.spaghetticodegang.trylater.recommendation.assignment.RecommendationAssignmentStatus;
import com.spaghetticodegang.trylater.recommendation.assignment.dto.RecommendationAssignmentStatusRequestDto;
import com.spaghetticodegang.trylater.recommendation.dto.RecommendationRequestDto;
import com.spaghetticodegang.trylater.recommendation.dto.RecommendationResponseDto;
import com.spaghetticodegang.trylater.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller providing endpoints for managing recommendations.
 */
@RestController
@RequestMapping("/api/recommendation")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * Handles a new recommendation request by delegating to the service layer.
     *
     * @param me      the currently authenticated user (requester)
     * @param request the recommendation request data including receiver user IDs
     * @return the created recommendation as a response DTO
     */
    @PostMapping
    public ResponseEntity<RecommendationResponseDto> createRecommendation(@AuthenticationPrincipal User me, @RequestBody @Valid RecommendationRequestDto request) {
        RecommendationResponseDto recommendationResponseDto = recommendationService.createRecommendation(me, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(recommendationResponseDto);
    }

    /**
     * Handles a recommendation assignment status update request by delegating to the service layer.
     *
     * @param me                             the currently authenticated user (requester)
     * @param recommendationAssignmentId     the ID of the recommendation assignment to update
     * @param recommendationAssignmentStatus the recommendation assignment status
     * @return the created recommendation as a response DTO
     */
    @PatchMapping("/assignment/{id}")
    public ResponseEntity<RecommendationResponseDto> updateRecommendationAssignmentStatus(@AuthenticationPrincipal User me, @PathVariable("id") Long recommendationAssignmentId, @RequestBody @Valid RecommendationAssignmentStatusRequestDto recommendationAssignmentStatus) {
        RecommendationResponseDto recommendationResponseDto = recommendationService.updateRecommendationAssignmentStatus(me, recommendationAssignmentId, recommendationAssignmentStatus);
        return ResponseEntity.ok(recommendationResponseDto);
    }

    /**
     * Handles request to get all assigned recommendations by delegating to the service layer.
     *
     * @param me the currently authenticated user (requester)
     * @param recommendationAssignmentStatus the status of the assignment
     * @return a list of all the assigned recommendation with that specified status
     */
    @GetMapping("/assignment")
    public ResponseEntity<List<RecommendationResponseDto>> getAllRecommendations(@AuthenticationPrincipal User me, @RequestParam(name = "status", required = false) RecommendationAssignmentStatus recommendationAssignmentStatus) {
        return ResponseEntity.ok(recommendationService.getAllRecommendationsByUserAndRecommendationStatus(me, recommendationAssignmentStatus));
    }

     /**
     * Deletes a recommendation assignment by delegating to the service layer.
     *
     * @param me                             the currently authenticated user (requester)
     * @param recommendationId     the ID of the recommendation that assignment should delete
     * @return A {@link ResponseEntity} with HTTP status code 204 (NO_CONTENT)
     */
    @DeleteMapping("/assignment/{id}")
    public ResponseEntity<Void> deleteRecommendationAssignment(@AuthenticationPrincipal User me, @PathVariable("id") Long recommendationId) {
        recommendationService.deleteRecommendationAssignment(me, recommendationId);
        return ResponseEntity.noContent().build();
    }
}
