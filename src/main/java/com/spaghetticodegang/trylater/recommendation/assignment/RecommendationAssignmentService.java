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

    /**
     * Performs validation and updates the recommendation assignment's status, including setting the acceptance date if applicable.
     *
     * @param me                                       the currently authenticated user
     * @param recommendationAssignmentId               the ID of the recommendation assignment whose status is to be updated
     * @param recommendationAssignmentStatusRequestDto the DTO containing the new recommendation assignment status
     * @return a response DTO representing the updated recommendation assignment
     * @throws ValidationException if the status change is invalid
     */
    public Long updateRecommendationAssignmentStatus(User me, Long recommendationAssignmentId, RecommendationAssignmentStatusRequestDto recommendationAssignmentStatusRequestDto) {
        final RecommendationAssignmentStatus recommendationAssignmentStatus = recommendationAssignmentStatusRequestDto.getRecommendationAssignmentStatus();
        final RecommendationAssignment recommendationAssignment = getRecommendationAssignmentById(recommendationAssignmentId);

        if (!Objects.equals(recommendationAssignment.getReceiver().getId(), me.getId())) {
            throw new ValidationException(Map.of("recommendationAssignment", messageUtil.get("recommendation.assignment.error.user.not.allowed")));
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

    /**
     * Finds a recommendation by its unique ID
     *
     * @param recommendationAssignmentId the ID of the recommendation assignment
     * @return the recommendation assignment entity
     * @throws RecommendationAssignmentNotFoundException if the recommendation assignment is not found
     */
    public RecommendationAssignment getRecommendationAssignmentById(Long recommendationAssignmentId) {
        return recommendationAssignmentRepository.findById(recommendationAssignmentId)
                .orElseThrow(() -> new RecommendationAssignmentNotFoundException("recommendation.assignment.error.not.found"));

    }

    public void deleteRecommendationAssignmentByRecommendationId(Long userId, Long recommendationId) {
        RecommendationAssignment recommendationAssignment = getRecommendationAssignmentByUserIdAndRecommendationId(userId, recommendationId);
        if (recommendationAssignment == null) {
            throw new RecommendationAssignmentNotFoundException("recommendation.assignment.error.not.found");
        }
        recommendationAssignmentRepository.deleteById(recommendationAssignment.getId());
    }

    private RecommendationAssignment getRecommendationAssignmentByUserIdAndRecommendationId(Long userId, Long recommendationId) {
        return recommendationAssignmentRepository.findRecommendationAssignmentByUserIdAndRecommendationId(userId, recommendationId);
    }
}
