package com.spaghetticodegang.trylater.image;


import com.spaghetticodegang.trylater.image.dto.ImageUploadRequestDto;
import com.spaghetticodegang.trylater.image.dto.ImageUploadResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

/**
 * REST controller providing endpoints for managing image handling.
 * This controller exposes API endpoints for uploading, uploading with scaling,
 * and deleting images. All requests are mapped under the "/api/images" base path.
 */
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    /**
     * Handles the upload of a single image file.
     * This endpoint accepts a multipart file named "image" and delegates
     * the processing to the {@link ImageService#uploadImage(MultipartFile)} method.
     * Upon successful upload, it returns a {@link ResponseEntity} with an
     * {@link ImageUploadResponseDto} in the body and an HTTP status code 201 (CREATED).
     *
     * @param request The {@link ImageUploadRequestDto} containing the image to be uploaded.
     * This parameter is required and must be named "imageFile" in the request.
     * @return A {@link ResponseEntity} containing an {@link ImageUploadResponseDto}
     * with the ID and path of the uploaded image and an HTTP status code 201 (CREATED).
     */
    @PostMapping
    public ResponseEntity<ImageUploadResponseDto> uploadImage(@Valid @ModelAttribute ImageUploadRequestDto request) {
        ImageUploadResponseDto imageUploadResponseDto = imageService.uploadImage(request.getImageFile());
        return ResponseEntity.status(HttpStatus.CREATED).body(imageUploadResponseDto);
    }

    /**
     * Handles the deletion of an image based on its unique ID.
     * This endpoint accepts the image ID as a query parameter named "imageId"
     * and delegates the deletion process to the {@link ImageService#deleteImageById(String)} method.
     * If the image is successfully deleted, it returns a {@link ResponseEntity}
     * with an HTTP status code 204 (NO_CONTENT). If the image with the given ID
     * is not found, it returns a {@link ResponseEntity} with an HTTP status code 404 (NOT_FOUND).
     *
     * @param id The unique identifier of the image to be deleted, provided as a query parameter named "imageId".
     * Must not be {@code null} or empty.
     * @return A {@link ResponseEntity} with HTTP status code 204 (NO_CONTENT) if the image was
     * successfully deleted, or 404 (NOT_FOUND) if the image with the given ID does not exist.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable("id") String id) {
        boolean isDeleted = imageService.deleteImageById(id);

        if (!isDeleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();

    }
}

