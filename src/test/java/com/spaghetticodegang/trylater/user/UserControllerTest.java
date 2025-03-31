package com.spaghetticodegang.trylater.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spaghetticodegang.trylater.shared.util.MessageUtil;
import com.spaghetticodegang.trylater.user.dto.UserMeRegistrationDto;
import com.spaghetticodegang.trylater.user.dto.UserMeResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private MessageUtil messageUtil;


    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturn200AndUser_whenRegistrationIsValid() throws Exception {
        UserMeResponseDto responseDto = UserMeResponseDto.builder()
                .id(1L)
                .userName("tester")
                .displayName("tester")
                .email("test@example.com")
                .imgPath("/assets/user.webp")
                .build();

        when(userService.registerUser(any())).thenReturn(responseDto);

        UserMeRegistrationDto requestDto = new UserMeRegistrationDto();
        requestDto.setUserName("tester");
        requestDto.setDisplayName("tester");
        requestDto.setEmail("test@example.com");
        requestDto.setPassword("secure123");

        mockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userName").value("tester"))
                .andExpect(jsonPath("$.displayName").value("tester"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.imgPath").value("/assets/user.webp"));
    }
}