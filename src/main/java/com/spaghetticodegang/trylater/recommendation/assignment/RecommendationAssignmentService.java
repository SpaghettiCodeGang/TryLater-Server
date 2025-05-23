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
import java.util.List;
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
                .acceptedAt((Objects.equals(recommendation.getCreator().getId(), receiver.getId()) ? LocalDateTime.now() : null))
                .recommendationAssignmentStatus(Objects.equals(recommendation.getCreator().getId(), receiver.getId()) ? RecommendationAssignmentStatus.ACCEPTED : RecommendationAssignmentStatus.SENT)
                .build();

        recommendationAssignmentRepository.save(recommendationAssignment);
    }

    /**
     * Performs validation and updates the recommendation assignment's status, including setting the acceptance date if applicable.
     *
     * @param me                                       the currently authenticated user
     * @param recommendationId                         the ID of the recommendation whose assignment status is to be updated
     * @param recommendationAssignmentStatusRequestDto the DTO containing the new recommendation assignment status
     * @throws ValidationException if the status change is invalid
     */
    public void updateRecommendationAssignmentStatus(User me, Long recommendationId, RecommendationAssignmentStatusRequestDto recommendationAssignmentStatusRequestDto) {
        final RecommendationAssignmentStatus recommendationAssignmentStatus = recommendationAssignmentStatusRequestDto.getRecommendationAssignmentStatus();
        final RecommendationAssignment recommendationAssignment = getRecommendationAssignmentByUserIdAndRecommendationId(me.getId(), recommendationId);

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

    /**
     * Delegates the get request from the recommendation service to the assignment repository for a given user and assignment status.
     *
     * @param me                             the currently authenticated user
     * @param recommendationAssignmentStatus the given status for the assigned recommendations
     * @return a list with recommendation entities or an empty list
     */
    public List<Recommendation> getAllRecommendationsByUserAndAssignmentStatus(User me, RecommendationAssignmentStatus recommendationAssignmentStatus) {
        return recommendationAssignmentRepository.findRecommendationsByUserIdAndRecommendationAssignmentStatus(me.getId(), recommendationAssignmentStatus);
    }

    /**
     * Deletes a recommendation assignment by its user ID and recommendation ID.
     *
     * @param userId           the user ID
     * @param recommendationId the recommendation ID
     * @throws RecommendationAssignmentNotFoundException if there is no assignment for the given user and recommendation.
     */
    public void deleteRecommendationAssignmentByRecommendationId(Long userId, Long recommendationId) {
        RecommendationAssignment recommendationAssignment = getRecommendationAssignmentByUserIdAndRecommendationId(userId, recommendationId);
        if (recommendationAssignment == null) {
            throw new RecommendationAssignmentNotFoundException("recommendation.assignment.error.not.found");
        }
        recommendationAssignmentRepository.deleteById(recommendationAssignment.getId());
    }

    /**
     * Returns a {@link RecommendationAssignment} entity for a given user ID and recommendation ID.
     *
     * @param userId           the user ID
     * @param recommendationId the recommendation ID
     * @return A {@link RecommendationAssignment} entity
     */
    public RecommendationAssignment getRecommendationAssignmentByUserIdAndRecommendationId(Long userId, Long recommendationId) {
        return recommendationAssignmentRepository.findRecommendationAssignmentByUserIdAndRecommendationId(userId, recommendationId);
    }

    /**
     * Checks if there is an assignment for a given recommendation ID.
     *
     * @param recommendationId the recommendation ID
     * @return TRUE or FALSE
     */
    public boolean existsRecommendationInRecommendationAssignment(Long recommendationId) {
        return recommendationAssignmentRepository.existsRecommendationAssignmentByRecommendationId(recommendationId);
    }
}
