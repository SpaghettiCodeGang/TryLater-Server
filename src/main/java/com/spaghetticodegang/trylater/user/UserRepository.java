package com.spaghetticodegang.trylater.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for accessing and managing user entities in the database.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Retrieves a user by email or username.
     *
     * @param email the email to search for
     * @param userName the username to search for
     * @return an {@link Optional} containing the user if found, or empty if not
     */
    Optional<User> findByEmailOrUserName(String email, String userName);

    /**
     * Checks whether a user with the given email already exists.
     *
     * @param email the email address to check
     * @return {@code true} if a user with the given email exists, {@code false} otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Checks whether a user with the given username already exists.
     *
     * @param userName the username to check
     * @return {@code true} if a user with the given username exists, {@code false} otherwise
     */
    boolean existsByUserName(String userName);
}
