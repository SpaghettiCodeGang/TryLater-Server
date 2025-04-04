package com.spaghetticodegang.trylater.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserResponseDto {

    private Long id;
    private String userName;
    private String displayName;
    private String imgPath;

}
