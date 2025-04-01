package com.spaghetticodegang.trylater.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Duration;
import java.util.Date;

@Service
@Getter
public class JwtService {

    private final Key key;
    private final int expirationDays;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration-days}") int expirationDays) {
        byte[] keyBytes = secret.getBytes();
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationDays = expirationDays;
    }

    public String generateTokenWithUsername(String username) {
        long expirationMillis = Duration.ofDays(expirationDays).toMillis();
        Date expirationDate = new Date(System.currentTimeMillis() + expirationMillis);

        return Jwts.builder()
                .setSubject(username)
                .setExpiration(expirationDate)
                .signWith(key)
                .compact();
    }

    public String extractUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
