package com.code.pair.yazan.paircode.service;

import com.code.pair.yazan.paircode.domain.AccountCredentials;

/**
 * Authentication operations for User registration and login.
 */
public interface AuthService {

    /**
     * Registers a new User when the username is available.
     *
     * @param credentials username and password
     * @throws com.code.pair.yazan.paircode.exception.InvalidRequestException when username is taken
     */
    void register(AccountCredentials credentials);

    /**
     * Authenticates a User and returns a JWT.
     *
     * @param credentials username and password
     * @return Bearer token value without the prefix
     * @throws org.springframework.security.authentication.BadCredentialsException when credentials are invalid
     */
    String login(AccountCredentials credentials);

    /**
     * Logout for stateless JWT — client clears stored token.
     */
    void logout();
}
