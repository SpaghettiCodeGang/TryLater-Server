package com.spaghetticodegang.trylater.shared.exception;

/**
 * Thrown when a contact with the specified ID is not found.
 */
public class RecommendationNotFoundException extends RuntimeException {

    /**
     * Constructs a new {@code ContactNotFoundException} for the given contact ID.
     */
    public RecommendationNotFoundException(String msg) {
        super(msg);
    }
}
