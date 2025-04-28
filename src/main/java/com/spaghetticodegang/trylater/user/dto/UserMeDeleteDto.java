package com.spaghetticodegang.trylater.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserMeDeleteDto {

    @Size(min = 6, message = "{user.password.size}")
    private String password;

}
