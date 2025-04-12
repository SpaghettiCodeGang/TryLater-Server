package com.spaghetticodegang.trylater.recommendation;

import com.spaghetticodegang.trylater.recommendation.dto.RecommendationRequestDto;
import com.spaghetticodegang.trylater.recommendation.dto.RecommendationResponseDto;
import com.spaghetticodegang.trylater.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * @param me the currently authenticated user (requester)
     * @param request the recommendation request data including receiver user IDs
     * @return the created recommendation as a response DTO
     */
    @PostMapping
    public ResponseEntity<RecommendationResponseDto> createRecommendation(@AuthenticationPrincipal User me, @RequestBody @Valid RecommendationRequestDto request) {
        RecommendationResponseDto recommendationResponseDto = recommendationService.createRecommendation(me, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(recommendationResponseDto);
    }
}
