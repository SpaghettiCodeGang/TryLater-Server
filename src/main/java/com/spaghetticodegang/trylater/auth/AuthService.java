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

/**
 * Service layer for handling authentication-related business logic.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    /**
     * Response returned after successful authentication.
     *
     * @param token the generated JWT
     * @param user the authenticated user's public profile data
     */
    public record AuthResponseDto(String token, UserMeResponseDto user) {}

    /**
     * Authenticates the user and returns a JWT along with user profile data.
     *
     * @param authRequestDto the login credentials
     * @return the JWT and the authenticated user's public information
     */
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