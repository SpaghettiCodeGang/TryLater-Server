package com.spaghetticodegang.trylater.recommendation.tag;

import com.spaghetticodegang.trylater.recommendation.taggroup.TagGroup;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for accessing and managing tag entities in the database.
 */
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * Checks whether a tag with the given tagName in a given tagGroup already exists.
     *
     * @param tagName the tagName to check
     * @param tagGroup the tagGroup to check
     * @return {@code true} if a tag with the given tagName in a given tagGroup exists, {@code false} otherwise
     */
    boolean existsByTagNameAndTagGroup(String tagName, TagGroup tagGroup);
}
