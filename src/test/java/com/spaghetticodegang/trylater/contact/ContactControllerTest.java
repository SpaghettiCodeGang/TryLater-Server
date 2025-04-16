package com.spaghetticodegang.trylater.contact;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spaghetticodegang.trylater.contact.dto.ContactRequestDto;
import com.spaghetticodegang.trylater.contact.dto.ContactResponseDto;
import com.spaghetticodegang.trylater.contact.dto.ContactStatusRequestDto;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    public static User createMockUser() {
        return User.builder()
                .id(1L)
                .userName("tester")
                .build();
    }

    public static UserResponseDto createPartnerDto() {
        return UserResponseDto.builder()
                .id(2L)
                .userName("partner")
                .displayName("partner")
                .imgPath("/assets/user.webp")
                .build();
    }

    public static ContactResponseDto createContactResponse(ContactStatus status) {
        return ContactResponseDto.builder()
                .contactId(1L)
                .contactPartner(createPartnerDto())
                .contactStatus(status)
                .build();
    }

    @BeforeEach
    void setup() {
        var auth = new UsernamePasswordAuthenticationToken(createMockUser(), null, List.of());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void shouldReturn201_whenContactCreatedSuccessfully() throws Exception {
        ContactRequestDto requestDto = new ContactRequestDto();
        requestDto.setTargetUserId(2L);

        when(contactService.createContact(any(User.class), any(ContactRequestDto.class)))
                .thenReturn(createContactResponse(ContactStatus.PENDING));

        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contactId").value(1L))
                .andExpect(jsonPath("$.contactPartner.userName").value("partner"))
                .andExpect(jsonPath("$.contactStatus").value("PENDING"));
    }

    @Test
    void shouldReturn200_whenContactUpdatedSuccessfully() throws Exception {
        ContactStatusRequestDto requestDto = new ContactStatusRequestDto();
        requestDto.setContactStatus(ContactStatus.ACCEPTED);

        when(contactService.updateContactStatus(any(User.class), any(Long.class) , any(ContactStatusRequestDto.class)))
                .thenReturn(createContactResponse(ContactStatus.ACCEPTED));

        mockMvc.perform(patch("/api/contact/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.contactId").value(1L))
                .andExpect(jsonPath("$.contactPartner.userName").value("partner"))
                .andExpect(jsonPath("$.contactStatus").value("ACCEPTED"));
    }

    @Test
    void shouldReturn204_whenContactDeletedSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/contact/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn200_whenGetContactById() throws Exception {
        when(contactService.getContact(any(User.class), any(Long.class)))
                .thenReturn(createContactResponse(ContactStatus.ACCEPTED));

        mockMvc.perform(get("/api/contact/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactId").value(1L))
                .andExpect(jsonPath("$.contactPartner.userName").value("partner"))
                .andExpect(jsonPath("$.contactStatus").value("ACCEPTED"));
    }

    @Test
    void shouldReturn200_whenGetAllContactsWithoutStatus() throws Exception {
        List<ContactResponseDto> contactList = List.of(
                createContactResponse(ContactStatus.ACCEPTED),
                createContactResponse(ContactStatus.PENDING)
        );

        when(contactService.getAllContacts(any(User.class), any()))
                .thenReturn(contactList);

        mockMvc.perform(get("/api/contact")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].contactStatus").value("ACCEPTED"))
                .andExpect(jsonPath("$[1].contactStatus").value("PENDING"));
    }

    @Test
    void shouldReturn200_whenGetAllContactsWithValidStatus() throws Exception {
        List<ContactResponseDto> filtered = List.of(createContactResponse(ContactStatus.PENDING));

        when(contactService.getAllContacts(any(User.class), any(ContactStatus.class)))
                .thenReturn(filtered);

        mockMvc.perform(get("/api/contact")
                        .param("contactStatus", "PENDING")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].contactStatus").value("PENDING"));
    }

    @Test
    void shouldReturn400_whenGetAllContactsWithInvalidStatus() throws Exception {
        mockMvc.perform(get("/api/contact")
                        .param("contactStatus", "INVALID_STATUS")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

}
