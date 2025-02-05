package com.code.pair.yazan.paircode.service.impl;

import com.code.pair.yazan.paircode.domain.AccountCredentials;
import com.code.pair.yazan.paircode.domain.AppUser;
import com.code.pair.yazan.paircode.exception.InvalidRequestException;
import com.code.pair.yazan.paircode.service.AppUserService;
import com.code.pair.yazan.paircode.service.AuthService;
import com.code.pair.yazan.paircode.service.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Default {@link AuthService} implementation.
 */
@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final AppUserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public void register(AccountCredentials credentials) {
        if (!userService.availableUsername(credentials.username())) {
            throw new InvalidRequestException("Username is already taken");
        }
        AppUser user = AppUser.builder()
                .username(credentials.username())
                .password(passwordEncoder.encode(credentials.password()))
                .build();
        userService.save(user);
    }

    @Override
    public String login(AccountCredentials credentials) {
        UsernamePasswordAuthenticationToken credentialsToken = new UsernamePasswordAuthenticationToken(
                credentials.username(),
                credentials.password());
        Authentication auth = authenticationManager.authenticate(credentialsToken);
        return jwtService.getToken(auth.getName());
    }

    @Override
    public void logout() {
        // Stateless JWT: client clears stored token.
    }
}
