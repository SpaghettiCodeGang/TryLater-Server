package com.spaghetticodegang.trylater.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserMeResponseDto {

    private Long id;
    private String userName;
    private String displayName;
    private String email;
    private String imgPath;

}