package com.spaghetticodegang.trylater.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spaghetticodegang.trylater.auth.dto.AuthRequestDto;
import com.spaghetticodegang.trylater.shared.util.MessageUtil;
import com.spaghetticodegang.trylater.user.dto.UserMeResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private AuthCookieService authCookieService;

    @MockBean
    private MessageUtil messageUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturn200AndSetCookie_whenLoginIsSuccessful() throws Exception {
        AuthRequestDto requestDto = new AuthRequestDto();
        requestDto.setLoginName("tester");
        requestDto.setPassword("secure123");

        UserMeResponseDto responseDto = UserMeResponseDto.builder()
                .id(1L)
                .userName("tester")
                .displayName("tester")
                .email("tester@example.com")
                .imgPath("/assets/user.webp")
                .build();

        var authResponseDto = new AuthService.AuthResponseDto("mocked.jwt.token", responseDto);

        when(authService.login(any(AuthRequestDto.class))).thenReturn(authResponseDto);

        doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(0);
            response.addHeader("Set-Cookie", ResponseCookie.from("token", "mocked.jwt.token").build().toString());
            return null;
        }).when(authCookieService).applyAuthCookie(any(), any());

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userName").value("tester"))
                .andExpect(jsonPath("$.email").value("tester@example.com"))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("token=mocked.jwt.token")));
    }
}
