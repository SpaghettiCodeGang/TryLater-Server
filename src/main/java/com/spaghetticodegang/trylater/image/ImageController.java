package com.spaghetticodegang.trylater.image;


import com.spaghetticodegang.trylater.image.dto.ImageUploadResponseDto;
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
     * @param imageFile The {@link MultipartFile} containing the image to be uploaded.
     * This parameter is required and must be named "image" in the request.
     * @return A {@link ResponseEntity} containing an {@link ImageUploadResponseDto}
     * with the ID and path of the uploaded image and an HTTP status code 201 (CREATED).
     */
    @PostMapping
    public ResponseEntity<ImageUploadResponseDto> uploadImage(@RequestParam("image") MultipartFile imageFile) {
        ImageUploadResponseDto imageUploadResponseDto = imageService.uploadImage(imageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(imageUploadResponseDto);
    }

    /**
     * Handles the upload and scaling of an image file.
     * This endpoint accepts a multipart file named "image" along with the desired
     * target width and height as query parameters. It delegates the processing
     * to the {@link ImageService#uploadImageWithScaling(MultipartFile, int, int)} method.
     * A successful upload and scaling operation results in a {@link ResponseEntity}
     * with an {@link ImageUploadResponseDto} and an HTTP status code 201 (CREATED).
     *
     * @param imageFile    The {@link MultipartFile} containing the image to be uploaded and scaled.
     * This parameter is required and must be named "image" in the request.
     * @param targetWidth  The desired width of the scaled image, provided as a query parameter named "width".
     * Must be a positive integer.
     * @param targetHeight The desired height of the scaled image, provided as a query parameter named "height".
     * Must be a positive integer.
     * @return A {@link ResponseEntity} containing an {@link ImageUploadResponseDto}
     * with the ID and path of the uploaded and scaled image and an HTTP status code 201 (CREATED).
     */
    @PostMapping("/scaling")
    public ResponseEntity<ImageUploadResponseDto> uploadImageWithScaling(@RequestParam("image") MultipartFile imageFile, @RequestParam(name = "width") int targetWidth, @RequestParam(name = "height") int targetHeight) {
        ImageUploadResponseDto imageUploadResponseDto = imageService.uploadImageWithScaling(imageFile, targetWidth, targetHeight);
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
    @DeleteMapping
    public ResponseEntity<Void> deleteImage(@RequestParam("imageId") String id) {
        boolean isDeleted = imageService.deleteImageById(id);

        if (!isDeleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();

    }
}

