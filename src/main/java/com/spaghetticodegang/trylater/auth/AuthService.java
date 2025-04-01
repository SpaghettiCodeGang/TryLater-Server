package com.spaghetticodegang.trylater.auth;

import com.spaghetticodegang.trylater.auth.dto.AuthRequestDto;
import com.spaghetticodegang.trylater.security.JwtService;
import com.spaghetticodegang.trylater.user.User;
import com.spaghetticodegang.trylater.user.UserService;
import com.spaghetticodegang.trylater.user.dto.UserMeResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    public record AuthResponseDto(String token, UserMeResponseDto user) {}

    public AuthResponseDto login(AuthRequestDto authRequestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequestDto.getLoginName(),
                        authRequestDto.getPassword())
        );

        String token = jwtService.generateTokenWithUsername(authentication.getName());

        User user = (User) userService.loadUserByUsername(authentication.getName());
        UserMeResponseDto userMeResponseDto = userService.createUserMeResponseDto(user);

        return new AuthResponseDto(token, userMeResponseDto);
    }

}