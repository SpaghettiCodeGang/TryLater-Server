package com.spaghetticodegang.trylater.recommendation;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository interface for accessing and managing recommendation entities in the database.
 */
public interface RecommendationRepository extends CrudRepository<Recommendation, Long> {

    @Transactional
    @Modifying
    @Query("""
                UPDATE Recommendation r SET r.creator = null WHERE r.creator.id = :userId
            """)
    void updateCreator(Long userId);
}