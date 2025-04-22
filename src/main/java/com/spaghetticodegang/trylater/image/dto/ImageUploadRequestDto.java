package com.spaghetticodegang.trylater.image.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder
public class ImageUploadRequestDto {
    @NotNull(message = "{image.no.file}")
    private MultipartFile imageFile;
}
