package com.spaghetticodegang.trylater.image;

import com.spaghetticodegang.trylater.shared.exception.ImageHandleException;
import com.spaghetticodegang.trylater.shared.util.MessageUtil;
import com.spaghetticodegang.trylater.image.dto.ImageUploadResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Service layer for handling business logic related to image upload and deletion.
 * This service provides methods for uploading, scaling, and deleting images,
 * as well as creating response DTOs for image uploads.
 */
@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final MessageUtil messageUtil;

    @Value("${image.upload.dir}")
    private String uploadDir;

    /**
     * Creates an {@link ImageUploadResponseDto} from a given {@link Image} entity.
     * This DTO contains essential information about the uploaded image,
     * such as its unique ID and the full path where it is stored.
     *
     * @param image The {@link Image} entity representing the uploaded image.
     *              Must not be {@code null}.
     * @return An {@link ImageUploadResponseDto} containing the image ID and its storage path.
     */
    public ImageUploadResponseDto createImageUploadResponseDto(Image image) {
        return ImageUploadResponseDto.builder()
                .imgPath(image.getImgPath())
                .build();
    }

    /**
     * Uploads a single image file.
     * This method performs the following steps:
     * <ol>
     * <li>Validates the provided {@link MultipartFile} to ensure it is not empty
     * and has a supported image format (png, jpeg, jpg, webp).</li>
     * <li>Generates a unique filename for the image using UUID and the original file extension.</li>
     * <li>Creates the necessary directory structure on the server if it doesn't exist.</li>
     * <li>Transfers the uploaded image file to the designated storage location.</li>
     * <li>Creates a new {@link Image} entity with the generated filename and saves it
     * to the database using the {@link ImageRepository}.</li>
     * <li>Returns an {@link ImageUploadResponseDto} containing the ID and path of the newly uploaded image.</li>
     * </ol>
     *
     * @param imageFile The {@link MultipartFile} representing the image to be uploaded.
     *                  Must not be {@code null} or empty.
     * @return An {@link ImageUploadResponseDto} with the ID and path of the uploaded image.
     * @throws ImageHandleException If the provided file is empty, has an unsupported format,
     *                              or if an {@link IOException} occurs during file handling.
     */
    public ImageUploadResponseDto uploadImage(MultipartFile imageFile) {
        try {
            final String imageType = validateImage(imageFile);
            final String imageName = UUID.randomUUID() + "." + imageType;
            Path path = Paths.get(uploadDir, imageName);
            Files.createDirectories(path.getParent());
            imageFile.transferTo(path);

            final Image image = Image.builder()
                    .imgPath(imageName)
                    .build();

            imageRepository.save(image);

            return createImageUploadResponseDto(image);
        } catch (IOException e) {
            throw new ImageHandleException(Map.of("image", messageUtil.get("image.upload.error") + ": " + e.getMessage()));
        }
    }

    /**
     * Deletes an image file from the file system based on its unique ID.
     * This method attempts to delete the file located at the path constructed
     * using the configured upload directory and the provided {@code imgPath}.
     *
     * @param imgPath The unique identifier of the image to be deleted.
     *                This should match the filename (including extension) of the image file.
     * @return {@code true} if the image file was successfully deleted;
     * {@code false} if the file does not exist.
     * @throws ImageHandleException If an {@link IOException} occurs during the deletion process.
     */
    public boolean deleteImageByImgPath(String imgPath) {
        Path filePath = Paths.get(uploadDir, imgPath);
        if (!Files.exists(filePath)) {
            return false;
        }
        try {
            imageRepository.deleteById(imgPath);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new ImageHandleException(Map.of("image", messageUtil.get("image.delete.error") + e.getMessage()));
        }
    }

    /**
     * Extracts the file extension (image type) from a given filename.
     * The method looks for the last occurrence of a dot ('.') in the filename
     * and returns the substring after it, converted to lowercase.
     * If no dot is found, an empty string is returned.
     *
     * @param imageName The filename from which to extract the image type.
     *                  Should not be {@code null}.
     * @return The lowercase file extension (e.g., "png", "jpg"), or an empty string if no extension is found.
     */
    private static String getImageType(String imageName) {
        int dotIndex = imageName.lastIndexOf(".");
        if (dotIndex > 0) {
            return imageName.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * Validates a given {@link MultipartFile} to ensure it is not null or empty
     * and that its original filename has a supported image file extension (png, jpeg, jpg, webp).
     * If the file is invalid, an {@link ImageHandleException} is thrown.
     *
     * @param imageFile The {@link MultipartFile} to be validated.
     *                  Must not be {@code null}.
     * @return The lowercase image type (file extension) if the validation is successful.
     * @throws ImageHandleException If the file is null or empty, or if its filename
     *                              does not have a supported image file extension.
     */
    private String validateImage(MultipartFile imageFile) {
        final String imageType = getImageType(Objects.requireNonNull(imageFile.getOriginalFilename()));
        final Set<String> allowedTypesSet = new HashSet<>(Arrays.asList("png", "jpeg", "jpg", "webp"));

        if (imageType.isEmpty() || !allowedTypesSet.contains(imageType.toLowerCase())) {
            throw new ImageHandleException(Map.of("image", messageUtil.get("image.wrong.type")));
        }

        return imageType;
    }
}