package com.spaghetticodegang.trylater.recommendation.assignment;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for accessing and managing recommendation assignment  entities in the database.
 */
public interface RecommendationAssignmentRepository extends JpaRepository<RecommendationAssignment, Long> {}