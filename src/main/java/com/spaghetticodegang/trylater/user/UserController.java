package com.spaghetticodegang.trylater.user;

import com.spaghetticodegang.trylater.user.dto.UserMeResponseDto;
import com.spaghetticodegang.trylater.user.dto.UserMeRegistrationDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller providing endpoints for user management.
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserMeResponseDto> getUserMe(@AuthenticationPrincipal User user) {
        UserMeResponseDto userMeResponseDto = userService.createUserMeResponseDto(user);
        return ResponseEntity.ok(userMeResponseDto);
    }

    /**
     * Handles the user registration request by delegating to the service layer.
     *
     * @param userMeRegistrationDto the registration data provided by the user
     * @return the created user's public information
     */
    @PostMapping
    public ResponseEntity<UserMeResponseDto> registerUser(@RequestBody @Valid UserMeRegistrationDto userMeRegistrationDto) {
        UserMeResponseDto userMeResponseDto = userService.registerUser(userMeRegistrationDto);
        return ResponseEntity.ok(userMeResponseDto);
    }
}
