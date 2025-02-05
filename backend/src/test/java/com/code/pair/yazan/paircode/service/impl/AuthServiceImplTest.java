package com.code.pair.yazan.paircode.service.impl;

import com.code.pair.yazan.paircode.domain.AccountCredentials;
import com.code.pair.yazan.paircode.domain.AppUser;
import com.code.pair.yazan.paircode.exception.InvalidRequestException;
import com.code.pair.yazan.paircode.service.AppUserService;
import com.code.pair.yazan.paircode.service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AppUserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_whenUsernameAvailable_savesUser() {
        when(userService.availableUsername("alice")).thenReturn(true);
        when(passwordEncoder.encode("secret")).thenReturn("hashed");

        authService.register(new AccountCredentials("alice", "secret"));

        verify(userService).save(any(AppUser.class));
    }

    @Test
    void register_whenUsernameTaken_throwsInvalidRequest() {
        when(userService.availableUsername("alice")).thenReturn(false);

        assertThrows(InvalidRequestException.class,
                () -> authService.register(new AccountCredentials("alice", "secret")));
    }

    @Test
    void login_whenValidCredentials_returnsJwt() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("alice");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtService.getToken("alice")).thenReturn("token-123");

        String token = authService.login(new AccountCredentials("alice", "secret"));

        assertEquals("token-123", token);
    }

    @Test
    void login_whenInvalidCredentials_propagatesBadCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad"));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new AccountCredentials("alice", "wrong")));
    }
}
