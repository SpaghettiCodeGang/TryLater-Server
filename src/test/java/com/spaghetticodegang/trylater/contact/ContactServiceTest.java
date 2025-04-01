package com.spaghetticodegang.trylater.contact;

import com.spaghetticodegang.trylater.contact.dto.ContactRequestDto;
import com.spaghetticodegang.trylater.contact.dto.ContactResponseDto;
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

    @Test
    void shouldThrowValidationException_whenTargetUserIsSelf() {
        User me = User.builder().id(1L).build();
        ContactRequestDto request = new ContactRequestDto();
        request.setTargetUserId(1L);

        when(messageUtil.get("contact.error.self.add")).thenReturn("Du kannst dich nicht selbst als Kontakt hinzufügen.");

        ValidationException ex = assertThrows(ValidationException.class, () -> {
            contactService.createContact(me, request);
        });

        assertTrue(ex.getErrors().containsKey("targetUserId"));
        assertEquals("Du kannst dich nicht selbst als Kontakt hinzufügen.", ex.getErrors().get("targetUserId"));
    }

    @Test
    void shouldThrowValidationException_whenContactAlreadyExists() {
        User me = User.builder().id(1L).build();
        ContactRequestDto request = new ContactRequestDto();
        request.setTargetUserId(2L);

        when(contactRepository.existsByUserIds(1L, 2L)).thenReturn(true);
        when(messageUtil.get("contact.error.already.exists")).thenReturn("Der Kontakt besteht bereits oder wurde bereits angefragt.");

        ValidationException ex = assertThrows(ValidationException.class, () -> {
            contactService.createContact(me, request);
        });

        assertTrue(ex.getErrors().containsKey("targetUserId"));
        assertEquals("Der Kontakt besteht bereits oder wurde bereits angefragt.", ex.getErrors().get("targetUserId"));
    }

    @Test
    void shouldCreateContactSuccessfully() {
        User me = User.builder().id(1L).build();
        User target = User.builder().id(2L).build();
        ContactRequestDto request = new ContactRequestDto();
        request.setTargetUserId(2L);

        Contact savedContact = Contact.builder()
                .id(99L)
                .requester(me)
                .receiver(target)
                .contactStatus(ContactStatus.PENDING)
                .requestDate(LocalDateTime.now())
                .build();

        when(contactRepository.existsByUserIds(1L, 2L)).thenReturn(false);
        when(userService.findUserById(2L)).thenReturn(target);
        when(contactRepository.save(any(Contact.class))).thenReturn(savedContact);

        UserResponseDto contactPartnerDto = UserResponseDto.builder()
                .id(2L)
                .userName("target")
                .displayName("target")
                .imgPath("/assets/user.webp")
                .build();

        when(userService.createUserResponseDto(target)).thenReturn(contactPartnerDto);

        ContactResponseDto result = contactService.createContact(me, request);

        assertNotNull(result);
        assertEquals(contactPartnerDto, result.getContactPartner());
        assertEquals(ContactStatus.PENDING, result.getContactStatus());
    }

    @Test
    void shouldCreateContactResponseDtoWithCorrectPartner() {
        User me = User.builder().id(1L).build();
        User other = User.builder().id(2L).build();

        Contact contact = Contact.builder()
                .id(10L)
                .requester(other)
                .receiver(me)
                .contactStatus(ContactStatus.PENDING)
                .build();

        UserResponseDto contactPartnerDto = UserResponseDto.builder()
                .id(2L)
                .userName("partner")
                .displayName("Partner")
                .imgPath("/assets/user.webp")
                .build();

        when(userService.createUserResponseDto(other)).thenReturn(contactPartnerDto);

        ContactResponseDto result = contactService.createContactResponseDto(me, contact);

        assertEquals(10L, result.getContactId());
        assertEquals(ContactStatus.PENDING, result.getContactStatus());
        assertEquals(contactPartnerDto, result.getContactPartner());
    }
}
