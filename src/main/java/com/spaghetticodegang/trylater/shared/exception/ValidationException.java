package com.spaghetticodegang.trylater.shared.exception;

import lombok.Getter;

import java.util.Map;

/**
 * Custom exception used to indicate validation failures within business logic.
 */
@Getter
public class ValidationException extends RuntimeException {
    private final Map<String, String> errors;


    /**
     * Constructs a new {@code ValidationException} with the given validation errors.
     *
     * @param errors a map of field names to error messages representing validation failures
     */
    public ValidationException(Map<String, String> errors) {
        super("Validation failed");
        this.errors = errors;
    }
}
