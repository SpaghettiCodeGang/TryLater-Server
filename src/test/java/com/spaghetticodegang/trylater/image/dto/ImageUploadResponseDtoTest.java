package com.spaghetticodegang.trylater.image.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ImageUploadResponseDtoTest {

    @Test
    void testBuilderAndGetterSetter() {
        String imageId = "uniqueImageId";

        ImageUploadResponseDto responseDto = ImageUploadResponseDto.builder()
                .imageId(imageId)
                .build();

        assertNotNull(responseDto);
        assertEquals(imageId, responseDto.getImageId());

        String newImageId = "anotherId";
        responseDto.setImageId(newImageId);
        assertEquals(newImageId, responseDto.getImageId());
    }

    @Test
    void testNoArgsConstructorViaBuilder() {
        ImageUploadResponseDto responseDto = ImageUploadResponseDto.builder().build();

        assertNotNull(responseDto);
        assertNull(responseDto.getImageId());
    }
}