package com.spaghetticodegang.trylater.image.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ImageUploadResponseDtoTest {

    @Test
    void testBuilderAndGetterSetter() {
        String imageId = "uniqueImageId";
        String imagePath = "/path/to/image.png";

        ImageUploadResponseDto responseDto = ImageUploadResponseDto.builder()
                .imageId(imageId)
                .imagePath(imagePath)
                .build();

        assertNotNull(responseDto);
        assertEquals(imageId, responseDto.getImageId());
        assertEquals(imagePath, responseDto.getImagePath());

        String newImageId = "anotherId";
        String newImagePath = "/new/path/image.jpg";
        responseDto.setImageId(newImageId);
        responseDto.setImagePath(newImagePath);
        assertEquals(newImageId, responseDto.getImageId());
        assertEquals(newImagePath, responseDto.getImagePath());
    }

    @Test
    void testNoArgsConstructorViaBuilder() {
        ImageUploadResponseDto responseDto = ImageUploadResponseDto.builder().build();

        assertNotNull(responseDto);
        assertNull(responseDto.getImageId());
        assertNull(responseDto.getImagePath());
    }
}