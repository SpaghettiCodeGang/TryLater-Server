package com.spaghetticodegang.trylater.user;

import com.spaghetticodegang.trylater.user.dto.UserMeResponseDto;
import com.spaghetticodegang.trylater.user.dto.UserMeRegistrationDto;
import com.spaghetticodegang.trylater.user.dto.UserMeUpdateDto;
import com.spaghetticodegang.trylater.user.dto.UserResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    /**
     * Returns the public profile information of the currently authenticated user.
     *
     * @param me the currently authenticated user (injected by Spring Security)
     * @return the authenticated user's public information
     */
    @GetMapping("/me")
    public ResponseEntity<UserMeResponseDto> getUserMe(@AuthenticationPrincipal User me) {
        UserMeResponseDto userMeResponseDto = userService.createUserMeResponseDto(me);
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
        return ResponseEntity.status(HttpStatus.CREATED).body(userMeResponseDto);
    }

    /**
     * Searches for a user by userName or email.
     *
     * @param user for search criteria
     * @return the matching user`s public information
     */
    @GetMapping("/search")
    public ResponseEntity<UserResponseDto> searchUser(@RequestParam String user) {
        UserResponseDto userResponseDto = userService.searchUser(user);
        return ResponseEntity.ok(userResponseDto);
    }

    @PatchMapping("/me")
    public ResponseEntity<UserMeResponseDto> updateUserProfile(@AuthenticationPrincipal User me, @RequestBody @Valid UserMeUpdateDto userMeUpdateDto) {
        UserMeResponseDto userMeResponseDto = userService.updateUserProfile(me, userMeUpdateDto);
        return ResponseEntity.ok(userMeResponseDto);
    }
}
