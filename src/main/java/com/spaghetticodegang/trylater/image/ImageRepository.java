package com.spaghetticodegang.trylater.image;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for accessing and managing contact entities in the database.
 */
public interface ImageRepository extends JpaRepository<Image, String> {
}
