package com.spaghetticodegang.trylater.shared.exception;

import lombok.Getter;

import java.util.Map;

/**
 * Thrown when an image could not be handled.
 */
@Getter
public class ImageHandleException extends RuntimeException {
    private final Map<String, String> errors;

    /**
     * Constructs a new {@code ImageHandleException}.
     *
     * @param errors a map of field names to error messages representing image handle failures
     */
    public ImageHandleException(Map<String, String> errors) {
        super("Image handle failed");
        this.errors = errors;
    }

}
