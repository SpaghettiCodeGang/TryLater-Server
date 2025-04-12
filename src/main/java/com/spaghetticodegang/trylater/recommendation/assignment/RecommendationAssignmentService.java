package com.spaghetticodegang.trylater.recommendation.assignment;

import com.spaghetticodegang.trylater.recommendation.Recommendation;
import com.spaghetticodegang.trylater.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service layer for handling recommendation assignment business logic.
 */
@Service
@RequiredArgsConstructor
public class RecommendationAssignmentService {

    private final RecommendationAssignmentRepository recommendationAssignmentRepository;

    /**
     * Creates and saves a new {@link RecommendationAssignment} entity,
     * indicating that the recommendation was sent to a receiver.
     *
     * @param recommendation the recommendation that was sent
     * @param receiver the user who receives the recommendation
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
}
