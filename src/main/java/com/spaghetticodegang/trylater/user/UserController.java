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

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/me")
    public ResponseEntity<UserMeResponseDto> registerUser(@RequestBody @Valid UserMeRegistrationDto userMeRegistrationDto) {
        UserMeResponseDto userMeResponseDto = userService.registerUser(userMeRegistrationDto);
        return ResponseEntity.ok().body(userMeResponseDto);
    }
}
