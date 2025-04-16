package com.spaghetticodegang.trylater.recommendation.tag.group;

import com.spaghetticodegang.trylater.recommendation.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for accessing and managing tag group entities in the database.
 */
public interface TagGroupRepository extends JpaRepository<TagGroup, Long> {

    /**
     * Retrieves all tag groups by category.
     *
     * @param category the category to filter tag groups by
     * @return a list of tag groups matching the given category
     */
    List<TagGroup> findAllByCategory(Category category);

    /**
     * Retrieves a tag group by tagGroupName or category.
     *
     * @param tagGroupName the tagGroupName to search for
     * @param category the category to search for
     * @return an {@link Optional} containing the tagGroup if found, or empty if not
     */
    Optional<TagGroup> findByTagGroupNameAndCategory(String tagGroupName, Category category);
}
