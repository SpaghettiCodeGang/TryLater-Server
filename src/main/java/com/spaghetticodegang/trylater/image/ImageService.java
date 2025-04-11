package com.spaghetticodegang.trylater.image;

import com.spaghetticodegang.trylater.shared.exception.ImageHandleException;
import com.spaghetticodegang.trylater.shared.util.MessageUtil;
import com.spaghetticodegang.trylater.image.dto.ImageUploadResponseDto;
import lombok.RequiredArgsConstructor;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final MessageUtil messageUtil;

    @Value("${image.upload.dir}")
    private String uploadDir;

    public ImageUploadResponseDto createImageUploadResponseDto(Image image) {
        return ImageUploadResponseDto.builder()
                .imageId(image.getImageId())
                .imagePath(uploadDir + image.getImageId())
                .build();
    }

    public ImageUploadResponseDto uploadImage(MultipartFile imageFile) {
        try {
            final String imageType = validateImage(imageFile);
            final String imageName = UUID.randomUUID() + "." + imageType;
            Path path = Paths.get(uploadDir, imageName);
            Files.createDirectories(path.getParent());
            imageFile.transferTo(path);

            final Image image = Image.builder()
                    .imageId(imageName)
                    .build();

            imageRepository.save(image);

            return createImageUploadResponseDto(image);
        } catch (IOException e) {
            throw new ImageHandleException(Map.of("image", messageUtil.get("image.upload.error") + ": " + e.getMessage()));
        }
    }

    public ImageUploadResponseDto uploadImageWithScaling(MultipartFile imageFile, int targetWidth, int targetHeight) {
        try {
            final String imageType = validateImage(imageFile);
            final String imageName = UUID.randomUUID() + "." + imageType;
            Path path = Paths.get(uploadDir, imageName);
            Files.createDirectories(path.getParent());
            BufferedImage originalImage = ImageIO.read(imageFile.getInputStream());
            if (originalImage == null) {
                throw new ImageHandleException(Map.of("image", messageUtil.get("image.upload.error") + messageUtil.get("image.upload.read.error")));
            }
            BufferedImage resizedImage = resizeImage(originalImage, targetWidth, targetHeight);
            ImageIO.write(resizedImage, imageType, path.toFile());

            final Image image = Image.builder()
                    .imageId(imageName)
                    .build();

            imageRepository.save(image);

            return createImageUploadResponseDto(image);
        } catch (IOException e) {
            throw new ImageHandleException(Map.of("image", messageUtil.get("image.upload.error") + e.getMessage()));
        }
    }

    public boolean deleteImageById(String uuid) {
        Path filePath = Paths.get(uploadDir, uuid);
        if (!Files.exists(filePath)) {
            return false;
        }
        try {
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new ImageHandleException(Map.of("image", messageUtil.get("image.delete.error") + e.getMessage()));
        }
    }

    private static String getImageType(String imageName) {
        int dotIndex = imageName.lastIndexOf(".");
        if (dotIndex > 0) {
            return imageName.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        return Scalr.resize(originalImage, Scalr.Mode.FIT_EXACT, targetWidth, targetHeight);
    }

    private String validateImage(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new ImageHandleException(Map.of("image", messageUtil.get("image.is.empty")));
        }

        final String imageType = getImageType(Objects.requireNonNull(imageFile.getOriginalFilename()));
        final Set<String> allowedTypesSet = new HashSet<>(Arrays.asList("png", "jpeg", "jpg", "webp"));

        if (imageType.isEmpty() || !allowedTypesSet.contains(imageType.toLowerCase())) {
            throw new ImageHandleException(Map.of("image", messageUtil.get("image.wrong.type")));
        }

        return imageType;
    }
}