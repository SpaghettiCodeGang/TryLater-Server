package com.spaghetticodegang.trylater.contact;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spaghetticodegang.trylater.contact.dto.ContactRequestDto;
import com.spaghetticodegang.trylater.contact.dto.ContactResponseDto;
import com.spaghetticodegang.trylater.shared.util.MessageUtil;
import com.spaghetticodegang.trylater.user.User;
import com.spaghetticodegang.trylater.user.dto.UserResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContactController.class)
@AutoConfigureMockMvc(addFilters = false)
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactService contactService;

    @MockBean
    private MessageUtil messageUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private User mockUser;

    @BeforeEach
    void setup() {
        mockUser = User.builder()
                .id(1L)
                .userName("tester")
                .build();

        var auth = new UsernamePasswordAuthenticationToken(mockUser, null, List.of());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void shouldReturn201_whenContactCreatedSuccessfully() throws Exception {
        ContactRequestDto requestDto = new ContactRequestDto();
        requestDto.setTargetUserId(2L);

        UserResponseDto partnerDto = UserResponseDto.builder()
                .id(2L)
                .userName("partner")
                .displayName("Partner")
                .imgPath("/assets/user.webp")
                .build();

        ContactResponseDto responseDto = ContactResponseDto.builder()
                .contactPartner(partnerDto)
                .contactStatus(ContactStatus.PENDING)
                .build();

        when(contactService.createContact(any(User.class), any(ContactRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contactPartner.userName").value("partner"))
                .andExpect(jsonPath("$.contactStatus").value("PENDING"));
    }
}
