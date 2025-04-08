package com.spaghetticodegang.trylater.contact;

import com.spaghetticodegang.trylater.contact.dto.ContactRequestDto;
import com.spaghetticodegang.trylater.contact.dto.ContactResponseDto;
import com.spaghetticodegang.trylater.contact.dto.ContactStatusRequestDto;
import com.spaghetticodegang.trylater.shared.exception.ValidationException;
import com.spaghetticodegang.trylater.shared.util.MessageUtil;
import com.spaghetticodegang.trylater.user.User;
import com.spaghetticodegang.trylater.user.dto.UserResponseDto;
import com.spaghetticodegang.trylater.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private UserService userService;

    @Mock
    private MessageUtil messageUtil;

    @InjectMocks
    private ContactService contactService;

    private User createUser(Long id) {
        return User.builder()
                .id(id)
                .userName("user" + id)
                .build();
    }

    private ContactRequestDto createContactRequest(Long targetId) {
        ContactRequestDto dto = new ContactRequestDto();
        dto.setTargetUserId(targetId);
        return dto;
    }

    private ContactStatusRequestDto createContactStatusRequest(ContactStatus status) {
        ContactStatusRequestDto dto = new ContactStatusRequestDto();
        dto.setContactStatus(status);
        return dto;
    }

    private Contact createContact(User requester, User receiver) {
        return Contact.builder()
                .id(99L)
                .requester(requester)
                .receiver(receiver)
                .contactStatus(ContactStatus.PENDING)
                .requestDate(LocalDateTime.now())
                .build();
    }

    private UserResponseDto createUserResponse(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .displayName(user.getUserName())
                .imgPath("/assets/user.webp")
                .build();
    }

    @Test
    void shouldThrowValidationException_whenTargetUserIsSelf() {
        User requester = createUser(1L);
        ContactRequestDto request = createContactRequest(1L);

        when(messageUtil.get("contact.error.self.add")).thenReturn("Du kannst dich nicht selbst als Kontakt hinzufügen.");

        ValidationException ex = assertThrows(ValidationException.class, () -> {
            contactService.createContact(requester, request);
        });

        assertTrue(ex.getErrors().containsKey("targetUserId"));
        assertEquals("Du kannst dich nicht selbst als Kontakt hinzufügen.", ex.getErrors().get("targetUserId"));
    }

    @Test
    void shouldThrowValidationException_whenContactAlreadyExists() {
        User requester = createUser(1L);
        ContactRequestDto request = createContactRequest(2L);

        when(contactRepository.existsByUserIds(1L, 2L)).thenReturn(true);
        when(messageUtil.get("contact.error.already.exists")).thenReturn("Der Kontakt besteht bereits oder wurde bereits angefragt.");

        ValidationException ex = assertThrows(ValidationException.class, () -> {
            contactService.createContact(requester, request);
        });

        assertTrue(ex.getErrors().containsKey("targetUserId"));
        assertEquals("Der Kontakt besteht bereits oder wurde bereits angefragt.", ex.getErrors().get("targetUserId"));
    }

    @Test
    void shouldCreateContactSuccessfully() {
        User requester = createUser(1L);
        User receiver = createUser(2L);
        ContactRequestDto request = createContactRequest(2L);
        Contact savedContact = createContact(requester, receiver);
        UserResponseDto contactPartnerDto = createUserResponse(receiver);

        when(contactRepository.existsByUserIds(1L, 2L)).thenReturn(false);
        when(userService.findUserById(2L)).thenReturn(receiver);
        when(contactRepository.save(any(Contact.class))).thenReturn(savedContact);
        when(userService.createUserResponseDto(receiver)).thenReturn(contactPartnerDto);

        ContactResponseDto result = contactService.createContact(requester, request);

        assertNotNull(result);
        assertEquals(contactPartnerDto, result.getContactPartner());
        assertEquals(ContactStatus.PENDING, result.getContactStatus());
    }

    @Test
    void shouldUpdateContactStatusSuccessfully() {
        User requester = createUser(1L);
        User receiver = createUser(2L);
        Contact contact = createContact(requester, receiver);
        ContactStatusRequestDto requestDto = createContactStatusRequest(ContactStatus.ACCEPTED);
        UserResponseDto contactPartnerDto = createUserResponse(requester);

        when(contactRepository.findById(99L)).thenReturn(java.util.Optional.of(contact));
        when(contactRepository.save(any(Contact.class))).thenReturn(contact);
        when(userService.createUserResponseDto(requester)).thenReturn(contactPartnerDto);

        ContactResponseDto result = contactService.updateContactStatus(receiver, 99L, requestDto);

        assertNotNull(result);
        assertEquals(ContactStatus.ACCEPTED, result.getContactStatus());
        assertEquals(99L, result.getContactId());
        assertEquals(contactPartnerDto, result.getContactPartner());
        assertNotNull(contact.getAcceptDate());
    }


    @Test
    void shouldCreateContactResponseDtoWithCorrectPartner() {
        User me = createUser(1L);
        User target = createUser(2L);
        Contact contact = createContact(me, target);
        UserResponseDto contactPartnerDto = createUserResponse(target);

        when(userService.createUserResponseDto(target)).thenReturn(contactPartnerDto);

        ContactResponseDto result = contactService.createContactResponseDto(me, contact);

        assertEquals(99L, result.getContactId());
        assertEquals(ContactStatus.PENDING, result.getContactStatus());
        assertEquals(contactPartnerDto, result.getContactPartner());
    }
}
