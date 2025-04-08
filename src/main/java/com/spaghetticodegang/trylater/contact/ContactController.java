package com.spaghetticodegang.trylater.contact;

import com.spaghetticodegang.trylater.contact.dto.ContactRequestDto;
import com.spaghetticodegang.trylater.contact.dto.ContactResponseDto;
import com.spaghetticodegang.trylater.contact.dto.ContactStatusRequestDto;
import com.spaghetticodegang.trylater.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller providing endpoints for managing user contacts.
 */
@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    /**
     * Handles a new contact request by delegating to the service layer.
     *
     * @param me the currently authenticated user (requester)
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
     * @param me the currently authenticated user
     * @param contactId the ID of the contact whose status is to be updated
     * @param contactStatus the new contact status provided by the user
     * @return the updated contact as a response DTO
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ContactResponseDto> updateContactStatus(@AuthenticationPrincipal User me,@PathVariable("id") Long contactId, @RequestBody @Valid ContactStatusRequestDto contactStatus) {
        ContactResponseDto contactResponseDto = contactService.updateContactStatus(me, contactId, contactStatus);
        return ResponseEntity.ok(contactResponseDto);
    }
}
