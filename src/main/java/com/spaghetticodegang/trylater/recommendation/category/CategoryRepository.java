package com.spaghetticodegang.trylater.recommendation.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for accessing and managing category entities in the database.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Retrieves a category by categoryType.
     *
     * @param categoryType the categoryType to search for
     * @return an {@link Optional} containing the category if found, or empty if not
     */
    Optional<Category> findByCategoryType(CategoryType categoryType);
}
