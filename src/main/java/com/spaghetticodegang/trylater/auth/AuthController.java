package com.spaghetticodegang.trylater.auth;

import com.spaghetticodegang.trylater.auth.dto.AuthRequestDto;
import com.spaghetticodegang.trylater.user.User;
import com.spaghetticodegang.trylater.user.dto.UserMeResponseDto;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller providing endpoints for authentication management.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthCookieService authCookieService;
    private final AuthService authService;

    /**
     * Handles the user login request by delegating to the service layer.
     *
     * @param authRequestDto the credentials provided by the user
     * @param response the HTTP servlet response used to attach the auth cookie
     * @return the authenticated user's public information
     */
    @PostMapping("/login")
    public ResponseEntity<UserMeResponseDto> login(@RequestBody @Valid AuthRequestDto authRequestDto, HttpServletResponse response) {
        AuthService.AuthResponseDto authResponseDto = authService.login(authRequestDto);
        authCookieService.applyAuthCookie(response, authResponseDto.token());
        return ResponseEntity.ok(authResponseDto.user());
    }

    /**
     * Logs the user out by clearing the authentication cookie.
     *
     * @param user the currently authenticated user (automatically injected)
     * @param response the HTTP servlet response used to remove the auth cookie
     * @return a 204 No Content response
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal User user, HttpServletResponse response) {
        authCookieService.clearAuthCookie(response);
        return ResponseEntity.noContent().build();
    }

}
