package com.spaghetticodegang.trylater.contact.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactRequestDto {

    @NotNull
    private Long targetUserId;

}
