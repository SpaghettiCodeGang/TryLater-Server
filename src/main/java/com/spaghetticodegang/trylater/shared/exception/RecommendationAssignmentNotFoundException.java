package com.spaghetticodegang.trylater.shared.exception;

/**
 * Thrown when a r ecommendation assignment with the specified ID is not found.
 */
public class RecommendationAssignmentNotFoundException extends RuntimeException {

    /**
     * Constructs a new {@code RecommendationAssignmentNotFoundException} for the given contact ID.
     */
    public RecommendationAssignmentNotFoundException(String msg) {
        super(msg);
    }
}
