package com.spaghetticodegang.trylater.auth;

import com.spaghetticodegang.trylater.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Service layer for handling authentication-related cookie operations.
 */
@Service
@RequiredArgsConstructor
public class AuthCookieService {

    private final JwtService jwtService;

    /**
     * Creates a secure HTTP-only cookie from the given JWT.
     *
     * @param token the JWT used for authentication
     * @return the authentication cookie
     */
    public ResponseCookie createAuthCookie(String token) {
        return ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(jwtService.getExpirationDays()))
                .sameSite("Strict")
                .build();
    }

    /**
     * Creates an expired authentication cookie to clear the token from the client.
     *
     * @return the expired authentication cookie
     */
    public ResponseCookie createExpiredCookie() {
        return ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
    }

    /**
     * Applies the authentication cookie to the given HTTP response.
     *
     * @param response the HTTP servlet response
     * @param token the JWT to include in the cookie
     */
    public void applyAuthCookie(HttpServletResponse response, String token) {
        response.setHeader("Set-Cookie", createAuthCookie(token).toString());
    }

    /**
     * Applies an expired authentication cookie to remove the token on the client.
     *
     * @param response the HTTP servlet response
     */
    public void clearAuthCookie(HttpServletResponse response) {
        response.setHeader("Set-Cookie", createExpiredCookie().toString());
    }

}
