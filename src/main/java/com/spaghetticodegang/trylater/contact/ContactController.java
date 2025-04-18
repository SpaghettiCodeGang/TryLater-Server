package com.spaghetticodegang.trylater.contact;

import com.spaghetticodegang.trylater.contact.dto.ContactRequestDto;
import com.spaghetticodegang.trylater.contact.dto.ContactResponseDto;
import com.spaghetticodegang.trylater.contact.dto.ContactStatusRequestDto;
import com.spaghetticodegang.trylater.contact.enums.ContactRole;
import com.spaghetticodegang.trylater.contact.enums.ContactStatus;
import com.spaghetticodegang.trylater.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller providing endpoints for managing user contacts.
 */
@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    /**
     * Returns all contact or contact request for a user by delegating the request to the service layer.
     *
     * @param me            the currently authenticated user
     * @param contactStatus the contact status provided by the user
     * @param contactRole   the requester/receiver role provided by the user
     * @return the created contact as a response DTO
     */
    @GetMapping
    public ResponseEntity<List<ContactResponseDto>> getAllContactsByStatusAndRole(
            @AuthenticationPrincipal User me,
            @RequestParam(name = "status") ContactStatus contactStatus,
            @RequestParam(name = "role", required = false) ContactRole contactRole) {
        return ResponseEntity.ok(contactService.getAllContactsByStatusAndRole(me, contactStatus, contactRole));
    }

    /**
     * Returns a contact or contact request by delegating the request to the service layer.
     *
     * @param me        the currently authenticated user
     * @param contactId the ID of the contact whose is requested
     * @return the created contact as a response DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContactResponseDto> getContactById(@AuthenticationPrincipal User me, @PathVariable("id") Long contactId) {
        return ResponseEntity.ok(contactService.getContact(me, contactId));
    }

    /**
     * Handles a new contact request by delegating to the service layer.
     *
     * @param me      the currently authenticated user (requester)
     * @param request the contact request data including target user ID
     * @return the created contact as a response DTO
     */
    @PostMapping
    public ResponseEntity<ContactResponseDto> createContact(@AuthenticationPrincipal User me, @RequestBody @Valid ContactRequestDto request) {
        ContactResponseDto contactResponseDto = contactService.createContact(me, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(contactResponseDto);
    }

    /**
     * Updates the status of a contact by delegating the request to the service layer.
     *
     * @param me            the currently authenticated user
     * @param contactId     the ID of the contact whose status is to be updated
     * @param contactStatus the new contact status provided by the user
     * @return the updated contact as a response DTO
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ContactResponseDto> updateContactStatus(@AuthenticationPrincipal User me, @PathVariable("id") Long contactId, @RequestBody @Valid ContactStatusRequestDto contactStatus) {
        ContactResponseDto contactResponseDto = contactService.updateContactStatus(me, contactId, contactStatus);
        return ResponseEntity.ok(contactResponseDto);
    }

    /**
     * Deletes a contact or contact request by delegating the request to the service layer.
     *
     * @param me        the currently authenticated user
     * @param contactId the ID of the contact whose status is to be deleted
     * @return A {@link ResponseEntity} with HTTP status code 204 (NO_CONTENT)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@AuthenticationPrincipal User me, @PathVariable("id") Long contactId) {
        contactService.deleteContact(me, contactId);
        return ResponseEntity.noContent().build();
    }

}
