package com.code.pair.yazan.paircode.service.impl;

import com.code.pair.yazan.paircode.service.JwtService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

/**
 * Issues and validates JWT bearer tokens for authenticated Users.
 */
@Service
public class JwtServiceImpl implements JwtService {
    static final String PREFIX = "Bearer";
    static final long JWT_EXPIRATION_DURATION = 1800_000;

    static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String getToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_DURATION))
                .signWith(key)
                .compact();
    }

    public String getAuthUser(HttpServletRequest request) {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        return getAuthUser(token);
    }

    public String getAuthUser(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        String jwt = stripBearerPrefix(token);
        if (jwt.isBlank()) {
            return null;
        }

        try {
            return Jwts.parser()
                    .setSigningKey(key).build()
                    .parseClaimsJws(jwt)
                    .getBody().getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    private static String stripBearerPrefix(String token) {
        String trimmed = token.trim();
        if (trimmed.regionMatches(true, 0, PREFIX + " ", 0, PREFIX.length() + 1)) {
            return trimmed.substring(PREFIX.length() + 1).trim();
        }
        if (trimmed.regionMatches(true, 0, PREFIX, 0, PREFIX.length())) {
            return trimmed.substring(PREFIX.length()).trim();
        }
        return trimmed;
    }
}
