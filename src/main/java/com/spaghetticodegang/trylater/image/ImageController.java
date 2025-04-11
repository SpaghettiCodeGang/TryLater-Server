package com.spaghetticodegang.trylater.image;


import com.spaghetticodegang.trylater.image.dto.ImageUploadResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import java.util.Optional;


@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping
    public ResponseEntity<ImageUploadResponseDto> uploadImage(@RequestParam("image") MultipartFile imageFile) {
        ImageUploadResponseDto imageUploadResponseDto = imageService.uploadImage(imageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(imageUploadResponseDto);
    }

    @PostMapping("/scaling")
    public ResponseEntity<ImageUploadResponseDto> uploadImageWithScaling(@RequestParam("image") MultipartFile imageFile, @RequestParam(name = "width") int targetWidth, @RequestParam(name = "height") int targetHeight) {
        ImageUploadResponseDto imageUploadResponseDto = imageService.uploadImageWithScaling(imageFile, targetWidth, targetHeight);
        return ResponseEntity.status(HttpStatus.CREATED).body(imageUploadResponseDto);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteImage(@RequestParam("imageId") String id) {
        boolean isDeleted = imageService.deleteImageById(id);

        if (!isDeleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();

    }
}

