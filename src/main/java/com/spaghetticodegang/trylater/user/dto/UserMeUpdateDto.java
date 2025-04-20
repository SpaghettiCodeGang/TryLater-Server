package com.spaghetticodegang.trylater.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserMeUpdateDto {

    //@NotBlank(message = "{user.username.notblank}")
    @Size(min = 3, message = "{user.username.size}")
    private String userName;

    //@NotBlank(message = "{user.displayname.notblank}")
    private String displayName;

    //@NotBlank(message = "{user.email.notblank}")
    @Email(message = "{user.email.invalid}")
    private String email;

    //@NotBlank(message = "{user.password.notblank}")
    @Size(min = 6, message = "{user.password.size}")
    private String currentPassword;

    @Size(min = 6, message = "{user.password.size}")
    private String newPassword;

    private String imgPath;

}
