package com.spaghetticodegang.trylater.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserMeUpdateDto {

    @Size(min = 3, message = "{user.username.size}")
    private String userName;

    private String displayName;

    @Email(message = "{user.email.invalid}")
    private String email;

    @Size(min = 6, message = "{user.password.size}")
    private String currentPassword;

    @Size(min = 6, message = "{user.password.size}")
    private String newPassword;

    private String imgPath;

}
