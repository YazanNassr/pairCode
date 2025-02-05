package com.code.pair.yazan.paircode.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    @InjectMocks
    private JwtServiceImpl jwtService;

    @Test
    void getAuthUser_parsesBearerTokenWithSpace() {
        String token = jwtService.getToken("alice");

        assertEquals("alice", jwtService.getAuthUser("Bearer " + token));
    }

    @Test
    void getAuthUser_parsesAuthorizationHeaderFromRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = jwtService.getToken("bob");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        assertEquals("bob", jwtService.getAuthUser(request));
    }

    @Test
    void getToken_returnsNonBlankJwt() {
        assertNotNull(jwtService.getToken("carol"));
    }
}
