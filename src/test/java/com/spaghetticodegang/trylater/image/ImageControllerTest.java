package com.spaghetticodegang.trylater.image;

import com.spaghetticodegang.trylater.image.dto.ImageUploadRequestDto;
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
        ImageUploadRequestDto mockRequestDto = ImageUploadRequestDto.builder()
                .imageFile(mockImageFile)
                .build();

        mockRequestDto.setImageFile(mockImageFile);

        ImageUploadResponseDto mockResponseDto = ImageUploadResponseDto.builder()
                .imgPath("generatedId")
                .build();

        when(imageService.uploadImage(mockImageFile)).thenReturn(mockResponseDto);

        ResponseEntity<ImageUploadResponseDto> response = imageController.uploadImage(mockRequestDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockResponseDto, response.getBody());
        verify(imageService, times(1)).uploadImage(mockImageFile);
    }

    @Test
    void deleteImage_success() {
        String imgPathToDelete = "existingImgPath";
        when(imageService.deleteImageByImgPath(imgPathToDelete)).thenReturn(true);

        ResponseEntity<Void> response = imageController.deleteImage(imgPathToDelete);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(imageService, times(1)).deleteImageByImgPath(imgPathToDelete);
    }

    @Test
    void deleteImage_notFound() {
        String nonExistingImgPath = "nonExistingId";
        when(imageService.deleteImageByImgPath(nonExistingImgPath)).thenReturn(false);

        ResponseEntity<Void> response = imageController.deleteImage(nonExistingImgPath);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(imageService, times(1)).deleteImageByImgPath(nonExistingImgPath);
    }
}
