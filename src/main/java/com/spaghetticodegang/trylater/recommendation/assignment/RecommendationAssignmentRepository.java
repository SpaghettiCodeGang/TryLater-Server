package com.spaghetticodegang.trylater.recommendation.assignment;

import com.spaghetticodegang.trylater.recommendation.Recommendation;
import com.spaghetticodegang.trylater.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Repository interface for accessing and managing recommendation assignment  entities in the database.
 */
public interface RecommendationAssignmentRepository extends JpaRepository<RecommendationAssignment, Long> {

    @Query("""
    SELECT r FROM RecommendationAssignment r WHERE r.receiver.id = :userId AND r.recommendation.id = :recommendationId
    """)
    RecommendationAssignment findRecommendationAssignmentByUserIdAndRecommendationId(Long userId, Long recommendationId);
}