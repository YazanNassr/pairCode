package com.code.pair.yazan.paircode.exception;

/**
 * Thrown when a Project mutation conflicts with a hot editor session (see ADR-0004).
 */
public class ProjectConflictException extends RuntimeException {

    public ProjectConflictException(String message) {
        super(message);
    }
}
