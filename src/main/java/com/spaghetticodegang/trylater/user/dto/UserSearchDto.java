package com.spaghetticodegang.trylater.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dto for user search requests
 * used when search for user in "add-friend" section by username or email
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchDto {
    @NotBlank(message = "Die Suchleiste kann nicht leer sein!")
    private String searchTerm;
}



