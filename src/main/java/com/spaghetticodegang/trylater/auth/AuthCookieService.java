package com.spaghetticodegang.trylater.auth;

import com.spaghetticodegang.trylater.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthCookieService {

    private final JwtService jwtService;

    public ResponseCookie createAuthCookie(String token) {
        return ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(jwtService.getExpirationDays()))
                .sameSite("Strict")
                .build();
    }

    public ResponseCookie createExpiredCookie() {
        return ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
    }

    public void applyAuthCookie(HttpServletResponse response, String token) {
        response.setHeader("Set-Cookie", createAuthCookie(token).toString());
    }

    public void clearAuthCookie(HttpServletResponse response) {
        response.setHeader("Set-Cookie", createExpiredCookie().toString());
    }

}
