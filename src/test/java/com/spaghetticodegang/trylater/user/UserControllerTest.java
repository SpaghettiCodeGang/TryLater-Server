package com.spaghetticodegang.trylater.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spaghetticodegang.trylater.shared.util.MessageUtil;
import com.spaghetticodegang.trylater.user.dto.UserMeRegistrationDto;
import com.spaghetticodegang.trylater.user.dto.UserMeResponseDto;
import com.spaghetticodegang.trylater.user.dto.UserMeUpdateDto;
import com.spaghetticodegang.trylater.user.dto.UserResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private MessageUtil messageUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private User mockUser;
    private UserMeResponseDto responseDto;

    @BeforeEach
    void setup() {
        mockUser = User.builder()
                .id(1L)
                .userName("tester")
                .displayName("Tester")
                .email("tester@example.com")
                .password("irrelevant")
                .imgPath("/assets/user.webp")
                .registrationDate(LocalDateTime.now())
                .build();

        responseDto = UserMeResponseDto.builder()
                .id(mockUser.getId())
                .userName(mockUser.getUserName())
                .displayName(mockUser.getDisplayName())
                .email(mockUser.getEmail())
                .imgPath(mockUser.getImgPath())
                .build();

        var authentication = new UsernamePasswordAuthenticationToken(mockUser, null, List.of());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void shouldReturn200AndUserMe_whenAuthenticated() throws Exception {
        when(userService.createUserMeResponseDto(mockUser)).thenReturn(responseDto);

        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userName").value("tester"))
                .andExpect(jsonPath("$.displayName").value("Tester"))
                .andExpect(jsonPath("$.email").value("tester@example.com"))
                .andExpect(jsonPath("$.imgPath").value("/assets/user.webp"));
    }

    @Test
    void shouldReturn200AndUser_whenRegistrationIsValid() throws Exception {
        when(userService.registerUser(any())).thenReturn(responseDto);

        UserMeRegistrationDto requestDto = new UserMeRegistrationDto();
        requestDto.setUserName("tester");
        requestDto.setEmail("tester@example.com");
        requestDto.setPassword("secure123");

        mockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userName").value("tester"))
                .andExpect(jsonPath("$.displayName").value("Tester"))
                .andExpect(jsonPath("$.email").value("tester@example.com"))
                .andExpect(jsonPath("$.imgPath").value("/assets/user.webp"));
    }

    @Test
    void shouldReturn200AndUserDto_whenSearchIsSuccessful() throws Exception {
        var userResponseDto = UserResponseDto.builder()
                .id(1L)
                .userName("searchTest")
                .displayName("Search Test")
                .imgPath("/assets/user.webp")
                .build();

        when(userService.searchUser("searchTest")).thenReturn(userResponseDto);

        mockMvc.perform(get("/api/user/search")
                        .param("user", "searchTest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userName").value("searchTest"))
                .andExpect(jsonPath("$.displayName").value("Search Test"))
                .andExpect(jsonPath("$.imgPath").value("/assets/user.webp"));
    }

    @Test
    void shouldReturn200AndUpdatedUser_whenProfileUpdateIsValid() throws Exception {
        var updateDto = new UserMeUpdateDto();
        updateDto.setUserName("updatedTester");
        updateDto.setDisplayName("Updated Tester");
        updateDto.setEmail("updated@example.com");
        updateDto.setCurrentPassword("oldPass123");
        updateDto.setNewPassword("newPass456");
        updateDto.setImgPath("/assets/updated.webp");

        UserMeResponseDto updatedResponse = UserMeResponseDto.builder()
                .id(mockUser.getId())
                .userName(updateDto.getUserName())
                .displayName(updateDto.getDisplayName())
                .email(updateDto.getEmail())
                .imgPath(updateDto.getImgPath())
                .build();

        when(userService.updateUserProfile(any(User.class), any(UserMeUpdateDto.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(patch("/api/user/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userName").value("updatedTester"))
                .andExpect(jsonPath("$.displayName").value("Updated Tester"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.imgPath").value("/assets/updated.webp"));
    }

}