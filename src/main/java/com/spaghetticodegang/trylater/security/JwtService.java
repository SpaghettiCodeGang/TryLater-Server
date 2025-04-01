package com.spaghetticodegang.trylater.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Duration;
import java.util.Date;

/**
 * Service layer for generating and extracting JWT tokens.
 */
@Service
@Getter
public class JwtService {

    private final Key key;
    private final int expirationDays;

    /**
     * Initializes the JWT service with the secret key and token expiration period.
     *
     * @param secret the secret used to sign tokens
     * @param expirationDays the token validity duration in days
     */
    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration-days}") int expirationDays) {
        byte[] keyBytes = secret.getBytes();
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationDays = expirationDays;
    }

    /**
     * Generates a signed JWT token containing the given username as subject.
     *
     * @param username the username to include in the token
     * @return the generated JWT
     */
    public String generateTokenWithUsername(String username) {
        long expirationMillis = Duration.ofDays(expirationDays).toMillis();
        Date expirationDate = new Date(System.currentTimeMillis() + expirationMillis);

        return Jwts.builder()
                .setSubject(username)
                .setExpiration(expirationDate)
                .signWith(key)
                .compact();
    }

    /**
     * Extracts the username (subject) from a valid JWT token.
     *
     * @param token the JWT to parse
     * @return the username contained in the token
     */
    public String extractUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
