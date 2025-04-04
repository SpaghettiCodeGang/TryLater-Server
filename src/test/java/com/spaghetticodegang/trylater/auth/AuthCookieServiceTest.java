package com.spaghetticodegang.trylater.auth;

import com.spaghetticodegang.trylater.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AuthCookieServiceTest {

    private JwtService jwtService;
    private AuthCookieService authCookieService;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        authCookieService = new AuthCookieService(jwtService);
    }

    @Test
    void shouldCreateHttpOnlySecureCookieWithToken() {
        when(jwtService.getExpirationDays()).thenReturn(7);

        ResponseCookie cookie = authCookieService.createAuthCookie("jwt-token");

        assertThat(cookie.getName()).isEqualTo("token");
        assertThat(cookie.getValue()).isEqualTo("jwt-token");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isTrue();
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofDays(7));
        assertThat(cookie.getSameSite()).isEqualTo("Strict");
    }

    @Test
    void shouldCreateExpiredCookie() {
        ResponseCookie expired = authCookieService.createExpiredCookie();

        assertThat(expired.getName()).isEqualTo("token");
        assertThat(expired.getValue()).isEmpty();
        assertThat(expired.getMaxAge().getSeconds()).isZero();
    }

    @Test
    void shouldSetAuthCookieInResponseHeader() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(jwtService.getExpirationDays()).thenReturn(5);

        authCookieService.applyAuthCookie(response, "jwt-token");

        verify(response).setHeader(eq("Set-Cookie"), contains("jwt-token"));
    }

    @Test
    void shouldClearAuthCookieInResponseHeader() {
        HttpServletResponse response = mock(HttpServletResponse.class);

        authCookieService.clearAuthCookie(response);

        verify(response).setHeader(eq("Set-Cookie"), contains("Max-Age=0"));
    }
}
