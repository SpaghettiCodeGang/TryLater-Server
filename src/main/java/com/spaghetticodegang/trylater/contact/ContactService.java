package com.spaghetticodegang.trylater.contact;

import com.spaghetticodegang.trylater.contact.dto.ContactRequestDto;
import com.spaghetticodegang.trylater.contact.dto.ContactResponseDto;
import com.spaghetticodegang.trylater.contact.dto.ContactStatusRequestDto;
import com.spaghetticodegang.trylater.shared.exception.ContactNotFoundException;
import com.spaghetticodegang.trylater.shared.exception.ValidationException;
import com.spaghetticodegang.trylater.shared.util.MessageUtil;
import com.spaghetticodegang.trylater.user.User;
import com.spaghetticodegang.trylater.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
     * Checks whether a contact relationship exists between two users,
     * regardless of which user was the requester or receiver.
     *
     * @param userId1 the ID of the first user
     * @param userId2 the ID of the second user
     * @return true if a contact already exists between the two users, false otherwise
     */
    public boolean existsByUserIds(Long userId1, Long userId2) {
        return contactRepository.existsByUserIds(userId1, userId2);
    }

    /**
     * Finds a contact by their unique ID.
     *
     * @param contactId the ID of the contact
     * @return the contact entity
     * @throws ContactNotFoundException if the contact is not found
     */
    public Contact findContactById(Long contactId) {
        return contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("contact.error.not.found"));

    }

    /**
     * Creates a new contact request between the authenticated user and the target user.
     * Performs validation to prevent duplicate or self-referential contacts.
     *
     * @param me      the authenticated user initiating the request
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

    /**
     * Performs validation and updates the contact's status, including setting the acceptance date if applicable.
     *
     * @param me                      the currently authenticated user
     * @param contactId               the ID of the contact whose status is to be updated
     * @param contactStatusRequestDto the DTO containing the new contact status
     * @return a response DTO representing the updated contact
     * @throws ValidationException if the status change is invalid
     */
    public ContactResponseDto updateContactStatus(User me, Long contactId, ContactStatusRequestDto contactStatusRequestDto) {
        final ContactStatus contactStatus = contactStatusRequestDto.getContactStatus();
        final Contact contact = findContactById(contactId);

        if (!Objects.equals(contact.getRequester().getId(), me.getId()) && !Objects.equals(contact.getReceiver().getId(), me.getId())) {
            throw new ValidationException(Map.of("contact", messageUtil.get("contact.error.user.not.found")));
        }

        if (contactStatus == ContactStatus.PENDING) {
            throw new ValidationException(Map.of("contactStatus", messageUtil.get("contact.error.status.revert.to.pending")));
        }

        if (contact.getRequester().getId().equals(me.getId()) && contactStatus == ContactStatus.ACCEPTED) {
            throw new ValidationException(Map.of("contactStatus", messageUtil.get("contact.error.self.update.status")));
        }

        if (contactStatus == ContactStatus.ACCEPTED) {
            contact.setAcceptDate(LocalDateTime.now());
        }

        contact.setContactStatus(contactStatus);
        contactRepository.save(contact);

        return createContactResponseDto(me, contact);
    }

    /**
     * Creates a response DTO from a {@link Contact} entity.
     * Determines which user is the contact partner (not the authenticated user).
     *
     * @param me      the authenticated user
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
     * Deletes a {@link Contact} entity.
     * Determines which user is the contact partner (not the authenticated user).
     *
     * @param me        the authenticated user
     * @param contactId the contact id
     */
    public void deleteContact(User me, Long contactId) {
        final Contact contact = findContactById(contactId);
        if (!Objects.equals(contact.getRequester().getId(), me.getId()) && !Objects.equals(contact.getReceiver().getId(), me.getId())) {
            throw new ValidationException(Map.of("contact", messageUtil.get("contact.error.user.not.found")));
        }
        contactRepository.deleteById(contactId);
    }

    /**
     * Searches the contact repository for a given id.
     *
     * @param me        the authenticated user
     * @param contactId the contact id
     * @return a response DTO representing the contact
     * @throws ValidationException if the user is neither the requester nor receiver of the contact
     */
    public ContactResponseDto getContact(User me, Long contactId) {
        final Contact contact = findContactById(contactId);

        if (!Objects.equals(contact.getRequester().getId(), me.getId()) && !Objects.equals(contact.getReceiver().getId(), me.getId())) {
            throw new ValidationException(Map.of("contact", messageUtil.get("contact.error.user.not.found")));
        }

        return createContactResponseDto(me, contact);
    }

    /**
     * Searches the contact repository for all contact of the user.
     *
     * @param me the authenticated user
     * @return a list of response DTO representing the contacts optional filters
     */
    public List<ContactResponseDto> getAllContactsForUser(User me, ContactStatus contactStatus) {

        List<Contact> contacts = findContactsByUserId(me.getId(), contactStatus);

        return contacts.stream()
                .map(contact -> createContactResponseDto(me, contact))
                .collect(Collectors.toList());
    }

    /**
     * Finds all contacts by their user.
     *
     * @param userId the ID of the user
     * @param contactStatus the status of the contact
     * @return a list of the contact entities
     */
    public List<Contact> findContactsByUserId(Long userId, ContactStatus contactStatus) {
        if (contactStatus != null) {
            return contactRepository.findByUserIdAndContactStatus(userId, contactStatus);
        }
        return contactRepository.findByUserId(userId);
    }
}
