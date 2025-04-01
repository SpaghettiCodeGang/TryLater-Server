package com.spaghetticodegang.trylater.auth;

import com.spaghetticodegang.trylater.auth.dto.AuthRequestDto;
import com.spaghetticodegang.trylater.user.dto.UserMeResponseDto;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthCookieService authCookieService;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<UserMeResponseDto> login(@RequestBody @Valid AuthRequestDto authRequestDto, HttpServletResponse response) {
        AuthService.AuthResponseDto authResponseDto = authService.login(authRequestDto);
        authCookieService.applyAuthCookie(response, authResponseDto.token());
        return ResponseEntity.ok(authResponseDto.user());
    }

}
