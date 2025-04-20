package com.spaghetticodegang.trylater.shared.exception;

/**
 * Thrown when the password is missing or incorrect.
 */
public class PasswordErrorException extends RuntimeException {

    /**
     * Constructs a new {@code MissingPasswordException} for the given user.
     */
    public PasswordErrorException(String msg) {
        super(msg);
    }
}
