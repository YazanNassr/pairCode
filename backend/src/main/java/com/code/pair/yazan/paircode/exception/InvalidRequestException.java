package com.code.pair.yazan.paircode.exception;

/**
 * Thrown when request validation fails.
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
