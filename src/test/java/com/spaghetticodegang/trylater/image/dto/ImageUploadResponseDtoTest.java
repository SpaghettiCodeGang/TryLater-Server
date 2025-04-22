package com.spaghetticodegang.trylater.image.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ImageUploadResponseDtoTest {

    @Test
    void testBuilderAndGetterSetter() {
        String imageId = "uniqueImageId";

        ImageUploadResponseDto responseDto = ImageUploadResponseDto.builder()
                .imagePath(imageId)
                .build();

        assertNotNull(responseDto);
        assertEquals(imageId, responseDto.getImagePath());

        String newImageId = "anotherId";
        responseDto.setImagePath(newImageId);
        assertEquals(newImageId, responseDto.getImagePath());
    }

    @Test
    void testNoArgsConstructorViaBuilder() {
        ImageUploadResponseDto responseDto = ImageUploadResponseDto.builder().build();

        assertNotNull(responseDto);
        assertNull(responseDto.getImagePath());
    }
}