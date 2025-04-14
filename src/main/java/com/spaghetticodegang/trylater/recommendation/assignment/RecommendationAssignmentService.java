package com.spaghetticodegang.trylater.recommendation.assignment;

import com.spaghetticodegang.trylater.recommendation.Recommendation;
import com.spaghetticodegang.trylater.recommendation.assignment.dto.RecommendationAssignmentStatusRequestDto;
import com.spaghetticodegang.trylater.shared.exception.RecommendationAssignmentNotFoundException;
import com.spaghetticodegang.trylater.shared.exception.ValidationException;
import com.spaghetticodegang.trylater.shared.util.MessageUtil;
import com.spaghetticodegang.trylater.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Service layer for handling recommendation assignment business logic.
 */
@Service
@RequiredArgsConstructor
public class RecommendationAssignmentService {

    private final RecommendationAssignmentRepository recommendationAssignmentRepository;
    private final MessageUtil messageUtil;

    /**
     * Creates and saves a new {@link RecommendationAssignment} entity,
     * indicating that the recommendation was sent to a receiver.
     *
     * @param recommendation the recommendation that was sent
     * @param receiver       the user who receives the recommendation
     */
    public void createRecommendationAssignment(Recommendation recommendation, User receiver) {
        RecommendationAssignment recommendationAssignment = RecommendationAssignment.builder()
                .recommendation(recommendation)
                .receiver(receiver)
                .sentAt(LocalDateTime.now())
                .recommendationAssignmentStatus(RecommendationAssignmentStatus.SENT)
                .build();

        recommendationAssignmentRepository.save(recommendationAssignment);
    }

    public Long updateRecommendationAssignmentStatus(User me, Long recommendationAssignmentId, RecommendationAssignmentStatusRequestDto recommendationAssignmentStatusRequestDto) {
        final RecommendationAssignmentStatus recommendationAssignmentStatus = recommendationAssignmentStatusRequestDto.getRecommendationAssignmentStatus();
        final RecommendationAssignment recommendationAssignment = findRecommendationAssignmentById(recommendationAssignmentId);

        if (!Objects.equals(recommendationAssignment.getReceiver().getId(), me.getId())) {
            throw new ValidationException(Map.of("recommendationAssignment", messageUtil.get("recommendation.assignment.error.user.not.found")));
        }

        if (recommendationAssignmentStatus == RecommendationAssignmentStatus.SENT) {
            throw new ValidationException(Map.of("recommendationAssignmentStatus", messageUtil.get("recommendation.assignment.error.status.revert.to.sent")));
        }

        if (recommendationAssignmentStatus == RecommendationAssignmentStatus.ACCEPTED) {
            recommendationAssignment.setAcceptedAt(LocalDateTime.now());
        }

        recommendationAssignment.setRecommendationAssignmentStatus(recommendationAssignmentStatus);
        recommendationAssignmentRepository.save(recommendationAssignment);

        return recommendationAssignment.getId();
    }

    private RecommendationAssignment findRecommendationAssignmentById(Long recommendationAssignmentId) {
        return recommendationAssignmentRepository.findById(recommendationAssignmentId)
                .orElseThrow(() -> new RecommendationAssignmentNotFoundException("recommendation.assignment.error.not.found"));

    }
}
