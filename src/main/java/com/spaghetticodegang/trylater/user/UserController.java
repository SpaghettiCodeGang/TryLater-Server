package com.spaghetticodegang.trylater.user;

import com.spaghetticodegang.trylater.user.dto.UserMeResponseDto;
import com.spaghetticodegang.trylater.user.dto.UserMeRegistrationDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller providing endpoints for user management.
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
