package com.code.pair.yazan.paircode.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void handleNotFound_returns404() {
        ProblemDetail detail = handler.handleNotFound(new ProjectNotFoundException("p1"));
        assertEquals(HttpStatus.NOT_FOUND.value(), detail.getStatus());
    }

    @Test
    void handleAccessDenied_returns403() {
        ProblemDetail detail = handler.handleAccessDenied(new AccessDeniedException("denied"));
        assertEquals(HttpStatus.FORBIDDEN.value(), detail.getStatus());
    }

    @Test
    void handleConflict_returns409() {
        ProblemDetail detail = handler.handleConflict(new ProjectConflictException("hot"));
        assertEquals(HttpStatus.CONFLICT.value(), detail.getStatus());
    }

    @Test
    void handleBadCredentials_returns401() {
        ProblemDetail detail = handler.handleBadCredentials(
                new org.springframework.security.authentication.BadCredentialsException("bad"));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), detail.getStatus());
    }
}
