package com.spaghetticodegang.trylater.contact.dto;

import com.spaghetticodegang.trylater.contact.ContactStatus;
import com.spaghetticodegang.trylater.user.dto.UserResponseDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class ContactResponseDto {

    private Long contactId;
    private UserResponseDto contactPartner;
    private ContactStatus contactStatus;

}
