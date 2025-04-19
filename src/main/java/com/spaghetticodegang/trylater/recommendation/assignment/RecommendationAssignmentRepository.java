package com.spaghetticodegang.trylater.recommendation.assignment;

import com.spaghetticodegang.trylater.recommendation.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository interface for accessing and managing recommendation assignment  entities in the database.
 */
public interface RecommendationAssignmentRepository extends JpaRepository<RecommendationAssignment, Long> {

    @Query("""
            SELECT r.recommendation FROM RecommendationAssignment r WHERE r.receiver.id =: userId AND r.recommendationAssignmentStatus =: recommendationAssignmentStatus
            """)
    List<Recommendation> findRecommendationsByUserIdAndRecommendationAssignmentStatus(Long id, RecommendationAssignmentStatus recommendationAssignmentStatus);
}