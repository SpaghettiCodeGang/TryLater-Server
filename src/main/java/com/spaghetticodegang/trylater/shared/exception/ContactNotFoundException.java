package com.spaghetticodegang.trylater.shared.exception;

/**
 * Thrown when a contact with the specified ID is not found.
 */
public class ContactNotFoundException extends RuntimeException {

    /**
     * Constructs a new {@code ContactNotFoundException} for the given contact ID.
     */
    public ContactNotFoundException(String msg) {
        super(msg);
    }
}
