package com.spaghetticodegang.trylater.contact;

import com.spaghetticodegang.trylater.contact.dto.ContactRequestDto;
import com.spaghetticodegang.trylater.contact.dto.ContactResponseDto;
import com.spaghetticodegang.trylater.shared.exception.ValidationException;
import com.spaghetticodegang.trylater.shared.util.MessageUtil;
import com.spaghetticodegang.trylater.user.User;
import com.spaghetticodegang.trylater.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service layer for handling business logic related to user contacts.
 */
@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final UserService userService;
    private final MessageUtil messageUtil;

    /**
     * Creates a response DTO from a {@link Contact} entity.
     * Determines which user is the contact partner (not the authenticated user).
     *
     * @param me the authenticated user
     * @param contact the contact entity
     * @return a response DTO representing the contact
     */
    public ContactResponseDto createContactResponseDto(User me, Contact contact) {
        User contactPartner = contact.getRequester() == me ? contact.getReceiver() : contact.getRequester();

        return ContactResponseDto.builder()
                .contactId(contact.getId())
                .contactPartner(userService.createUserResponseDto(contactPartner))
                .contactStatus(contact.getContactStatus())
                .build();
    }

    /**
     * Creates a new contact request between the authenticated user and the target user.
     * Performs validation to prevent duplicate or self-referential contacts.
     *
     * @param me the authenticated user initiating the request
     * @param request the contact request containing the target user's ID
     * @return a response DTO representing the newly created contact
     * @throws ValidationException if the target is the same as the requester
     *                             or if a contact already exists between the users
     */
    public ContactResponseDto createContact(User me, ContactRequestDto request) {
        final Long targetUserId = request.getTargetUserId();

        if (me.getId().equals(targetUserId)) {
            throw new ValidationException(Map.of("targetUserId", messageUtil.get("contact.error.self.add")));
        }

        if (contactRepository.existsByUserIds(me.getId(), targetUserId)) {
            throw new ValidationException(Map.of("targetUserId", messageUtil.get("contact.error.already.exists")));
        }

        final User targetUser = userService.findUserById(targetUserId);
        final LocalDateTime now = LocalDateTime.now();

        final Contact contact = Contact.builder()
                .requester(me)
                .receiver(targetUser)
                .requestDate(now)
                .contactStatus(ContactStatus.PENDING)
                .build();

        contactRepository.save(contact);
        return createContactResponseDto(me, contact);
    }
}
