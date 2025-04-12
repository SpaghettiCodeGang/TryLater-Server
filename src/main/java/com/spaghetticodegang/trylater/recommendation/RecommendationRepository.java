package com.spaghetticodegang.trylater.recommendation;

import org.springframework.data.repository.CrudRepository;

/**
 * Repository interface for accessing and managing recommendation entities in the database.
 */
public interface RecommendationRepository extends CrudRepository<Recommendation, Long> {}