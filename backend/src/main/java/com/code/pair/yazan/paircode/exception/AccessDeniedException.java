package com.code.pair.yazan.paircode.exception;

/**
 * Thrown when a User attempts an owner-only action or is forbidden from mutating a Project.
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
