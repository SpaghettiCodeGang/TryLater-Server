package com.spaghetticodegang.trylater.image.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ImageUploadResponseDtoTest {

    @Test
    void testBuilderAndGetterSetter() {
        String imgPath = "uniqueImgPath";

        ImageUploadResponseDto responseDto = ImageUploadResponseDto.builder()
                .imgPath(imgPath)
                .build();

        assertNotNull(responseDto);
        assertEquals(imgPath, responseDto.getImgPath());

        String newImgPath = "anotherId";
        responseDto.setImgPath(newImgPath);
        assertEquals(newImgPath, responseDto.getImgPath());
    }

    @Test
    void testNoArgsConstructorViaBuilder() {
        ImageUploadResponseDto responseDto = ImageUploadResponseDto.builder().build();

        assertNotNull(responseDto);
        assertNull(responseDto.getImgPath());
    }
}