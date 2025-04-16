package com.spaghetticodegang.trylater.shared.exception;

/**
 * Thrown when a recommendation with the specified ID is not found.
 */
public class RecommendationNotFoundException extends RuntimeException {

    /**
     * Constructs a new {@code RecommendationNotFoundException} for the given contact ID.
     */
    public RecommendationNotFoundException(String msg) {
        super(msg);
    }
}
