package com.spaghetticodegang.trylater.image;

import com.spaghetticodegang.trylater.image.dto.ImageUploadResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImageControllerTest {

    @InjectMocks
    private ImageController imageController;

    @Mock
    private ImageService imageService;

    @Test
    void uploadImage_success() {
        MultipartFile mockImageFile = new MockMultipartFile("image", "test.png", "image/png", "some image data".getBytes());
        ImageUploadResponseDto mockResponseDto = ImageUploadResponseDto.builder()
                .imageId("generatedId")
                .imagePath("/path/to/generatedId.png")
                .build();

        when(imageService.uploadImage(mockImageFile)).thenReturn(mockResponseDto);

        ResponseEntity<ImageUploadResponseDto> response = imageController.uploadImage(mockImageFile);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockResponseDto, response.getBody());
        verify(imageService, times(1)).uploadImage(mockImageFile);
    }

    @Test
    void uploadImageWithScaling_success() {
        MultipartFile mockImageFile = new MockMultipartFile("image", "test.jpg", "image/jpeg", "other image data".getBytes());
        int targetWidth = 100;
        int targetHeight = 100;
        ImageUploadResponseDto mockResponseDto = ImageUploadResponseDto.builder()
                .imageId("scaledId")
                .imagePath("/path/to/scaledId_scaled.jpg")
                .build();

        when(imageService.uploadImageWithScaling(mockImageFile, targetWidth, targetHeight)).thenReturn(mockResponseDto);

        ResponseEntity<ImageUploadResponseDto> response = imageController.uploadImageWithScaling(mockImageFile, targetWidth, targetHeight);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockResponseDto, response.getBody());
        verify(imageService, times(1)).uploadImageWithScaling(mockImageFile, targetWidth, targetHeight);
    }

    @Test
    void deleteImage_success() {
        String imageIdToDelete = "existingImageId";
        when(imageService.deleteImageById(imageIdToDelete)).thenReturn(true);

        ResponseEntity<Void> response = imageController.deleteImage(imageIdToDelete);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(imageService, times(1)).deleteImageById(imageIdToDelete);
    }

    @Test
    void deleteImage_notFound() {
        String nonExistingImageId = "nonExistingId";
        when(imageService.deleteImageById(nonExistingImageId)).thenReturn(false);

        ResponseEntity<Void> response = imageController.deleteImage(nonExistingImageId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(imageService, times(1)).deleteImageById(nonExistingImageId);
    }
}
