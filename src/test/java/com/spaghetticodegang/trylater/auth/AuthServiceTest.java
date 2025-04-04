package com.spaghetticodegang.trylater.auth;

import com.spaghetticodegang.trylater.auth.dto.AuthRequestDto;
import com.spaghetticodegang.trylater.security.JwtService;
import com.spaghetticodegang.trylater.user.User;
import com.spaghetticodegang.trylater.user.UserService;
import com.spaghetticodegang.trylater.user.dto.UserMeResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private AuthenticationManager authenticationManager;
    private UserService userService;
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        userService = mock(UserService.class);
        jwtService = mock(JwtService.class);
        authService = new AuthService(authenticationManager, userService, jwtService);
    }

    @Test
    void shouldAuthenticateAndReturnTokenAndUser() {
        var authDto = new AuthRequestDto();
        authDto.setLoginName("tester");
        authDto.setPassword("secure123");

        var authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("tester");

        var user = User.builder().userName("tester").build();
        var userDto = UserMeResponseDto.builder().userName("tester").build();

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateTokenWithUsername("tester")).thenReturn("jwt-token");
        when(userService.loadUserByUsername("tester")).thenReturn(user);
        when(userService.createUserMeResponseDto(user)).thenReturn(userDto);

        var result = authService.login(authDto);

        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.user().getUserName()).isEqualTo("tester");

        ArgumentCaptor<UsernamePasswordAuthenticationToken> tokenCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);

        verify(authenticationManager).authenticate(tokenCaptor.capture());
        UsernamePasswordAuthenticationToken capturedToken = tokenCaptor.getValue();

        assertThat(capturedToken.getName()).isEqualTo("tester");
        assertThat(capturedToken.getCredentials()).isEqualTo("secure123");
    }
}
