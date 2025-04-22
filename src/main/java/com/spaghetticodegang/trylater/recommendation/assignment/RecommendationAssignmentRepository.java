package com.spaghetticodegang.trylater.recommendation.assignment;

import com.spaghetticodegang.trylater.recommendation.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


/**
 * Repository interface for accessing and managing recommendation assignment  entities in the database.
 */
public interface RecommendationAssignmentRepository extends JpaRepository<RecommendationAssignment, Long> {

    /**
     * Finds all assigned recommendations for a given user with a specific status.
     *
     * @param id the user ID
     * @param recommendationAssignmentStatus the assignment status
     * @return a list of recommendation entities or an empty list
     */
    @Query("""
            SELECT r.recommendation FROM RecommendationAssignment r WHERE r.receiver.id = :id AND r.recommendationAssignmentStatus = :recommendationAssignmentStatus
            """)
    List<Recommendation> findRecommendationsByUserIdAndRecommendationAssignmentStatus(Long id, RecommendationAssignmentStatus recommendationAssignmentStatus);

    /**       
     * Finds the recommendation assignment for the given user ID and recommendation ID.
     *
     * @param userId the given user ID
     * @param recommendationId the given recommendation ID
     * @return A {@link RecommendationAssignment} Entity
     */
    @Query("""
            SELECT r FROM RecommendationAssignment r WHERE r.receiver.id = :userId AND r.recommendation.id = :recommendationId
            """)
    RecommendationAssignment findRecommendationAssignmentByUserIdAndRecommendationId(Long userId, Long recommendationId);

    /**
     * Checks if a recommendation assignment for a given recommendation ID exists.
     *
     * @param recommendationId the ID of the recommendation
     * @return TRUE or FALSE
     */
    @Query("""
            SELECT CASE WHEN count(r) > 0 THEN true ELSE false END
            FROM RecommendationAssignment r
            WHERE r.recommendation.id = :recommendationId
            """)
    boolean existsRecommendationAssignmentByRecommendationId(Long recommendationId);
}