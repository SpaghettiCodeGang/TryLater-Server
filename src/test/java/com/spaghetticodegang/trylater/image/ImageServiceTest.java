package com.spaghetticodegang.trylater.image;

import com.spaghetticodegang.trylater.image.dto.ImageUploadResponseDto;
import com.spaghetticodegang.trylater.shared.exception.ImageHandleException;
import com.spaghetticodegang.trylater.shared.util.MessageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ImageServiceTest {

    @InjectMocks
    private ImageService imageService;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private MessageUtil messageUtil;

    @Captor
    private ArgumentCaptor<Image> imageArgumentCaptor;

    private final String TEST_UPLOAD_DIR = "test-upload/";
    private final String TEST_IMAGE_NAME = "test-image.png";
    private final byte[] TEST_IMAGE_DATA = "some image data".getBytes();

    private Path testUploadDirPath;

    @BeforeEach
    void setUp() throws IOException {
        ReflectionTestUtils.setField(imageService, "uploadDir", TEST_UPLOAD_DIR);
        testUploadDirPath = Paths.get(TEST_UPLOAD_DIR);
        if (!Files.exists(testUploadDirPath)) {
            Files.createDirectories(testUploadDirPath);
        }
    }

    private String getImageIdFromUpload(MultipartFile file) {
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
        String imageType = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        return UUID.randomUUID() + "." + imageType;
    }

    @Test
    void uploadImage_success() throws IOException {
        MultipartFile mockImageFile = new MockMultipartFile("image", TEST_IMAGE_NAME, "image/png", TEST_IMAGE_DATA);
        String fixedUuid = "a1b2c3d4-e5f6-7890-1234-567890abcdef";
        String expectedImageName = fixedUuid + ".png";
        String expectedImagePath = TEST_UPLOAD_DIR + expectedImageName;
        ArgumentCaptor<Image> imageCaptor = ArgumentCaptor.forClass(Image.class);
        UUID fixedUuidObject = UUID.fromString(fixedUuid);

        try (var mockStatic = mockStatic(UUID.class)) {
            mockStatic.when(UUID::randomUUID).thenReturn(fixedUuidObject);

            ImageUploadResponseDto responseDto = imageService.uploadImage(mockImageFile);

            assertNotNull(responseDto);
            assertEquals(expectedImagePath, responseDto.getImagePath());
            assertEquals(expectedImageName, responseDto.getImageId());

            verify(imageRepository, times(1)).save(imageCaptor.capture());
            assertEquals(expectedImageName, imageCaptor.getValue().getImageId());

            verify(messageUtil, never()).get(anyString());
            assertTrue(Files.exists(Paths.get(expectedImagePath)));
            Files.deleteIfExists(Paths.get(expectedImagePath));
        }
    }

    @Test
    void uploadImage_wrongType_throwsImageHandleException() {
        MultipartFile mockImageFile = new MockMultipartFile("image", "test-image.gif", "image/gif", TEST_IMAGE_DATA);
        when(messageUtil.get("image.wrong.type")).thenReturn("Image type is not allowed.");

        ImageHandleException exception = assertThrows(ImageHandleException.class, () -> imageService.uploadImage(mockImageFile));
        assertEquals("Image type is not allowed.", exception.getErrors().get("image"));

        verify(imageRepository, never()).save(any());
        verify(messageUtil, times(1)).get("image.wrong.type");
        assertFalse(Files.exists(testUploadDirPath.resolve(getImageIdFromUpload(mockImageFile))));
    }

    @Test
    void uploadImage_transferToFails_throwsImageHandleException() throws IOException {
        MultipartFile mockImageFile = spy(new MockMultipartFile("image", TEST_IMAGE_NAME, "image/png", TEST_IMAGE_DATA));
        when(messageUtil.get("image.upload.error")).thenReturn("Failed to transfer file.");
        doThrow(new IOException("Failed to transfer file."))
                .when(mockImageFile).transferTo(any(Path.class));

        ImageHandleException exception = assertThrows(ImageHandleException.class, () -> imageService.uploadImage(mockImageFile));
        assertEquals("Failed to transfer file.: Failed to transfer file.", exception.getErrors().get("image"));

        verify(imageRepository, never()).save(any());
        verify(messageUtil, times(1)).get("image.upload.error");
        verify(mockImageFile, times(1)).transferTo(any(Path.class));
        assertFalse(Files.exists(testUploadDirPath.resolve(getImageIdFromUpload(mockImageFile))));
    }


    @Test
    void uploadImageWithScaling_validateImageFailsWrongType_throwsImageHandleException() throws IOException {
        MultipartFile mockImageFile = new MockMultipartFile("image", "test-image.gif", "image/gif", TEST_IMAGE_DATA);
        when(messageUtil.get("image.wrong.type")).thenReturn("Image type is not allowed.");

        ImageHandleException exception = assertThrows(ImageHandleException.class, () -> imageService.uploadImageWithScaling(mockImageFile, 100, 100));
        assertEquals("Image type is not allowed.", exception.getErrors().get("image"));

        verify(imageRepository, never()).save(any());
        verify(messageUtil, times(1)).get("image.wrong.type");
        assertFalse(Files.exists(testUploadDirPath.resolve(getImageIdFromUpload(mockImageFile))));
    }

    @Test
    void uploadImageWithScaling_imageIOReadFails_throwsImageHandleException() throws IOException {
        MultipartFile mockImageFile = new MockMultipartFile("image", TEST_IMAGE_NAME, "image/png", TEST_IMAGE_DATA);
        String fixedUuidString = "a1b2c3d4-e5f6-7890-1234-567890abcdef";
        UUID fixedUuid = UUID.fromString(fixedUuidString);
        when(messageUtil.get("image.upload.error")).thenReturn("Image upload failed.");
        when(messageUtil.get("image.upload.read.error")).thenReturn("Failed to read image data.");

        try (var mockStatic = mockStatic(UUID.class);
             var mockImageIOStatic = mockStatic(ImageIO.class)) {
            mockStatic.when(UUID::randomUUID).thenReturn(fixedUuid);
            mockImageIOStatic.when(() -> ImageIO.read(any(java.io.InputStream.class))).thenReturn(null);

            ImageHandleException exception = assertThrows(ImageHandleException.class,
                    () -> imageService.uploadImageWithScaling(mockImageFile, 100, 100));

            assertEquals("Image upload failed.Failed to read image data.", exception.getErrors().get("image"));

            verify(imageRepository, never()).save(any());
            verify(messageUtil, times(1)).get("image.upload.error");
            verify(messageUtil, times(1)).get("image.upload.read.error");
            assertFalse(Files.exists(Paths.get(TEST_UPLOAD_DIR + fixedUuidString + ".png")));
        }
    }

    @Test
    void deleteImageById_fileDeletesSuccessfully() throws IOException {
        String testUuid = "test-uuid.png";
        Path filePath = Paths.get(TEST_UPLOAD_DIR, testUuid);
        Files.createFile(filePath);
        when(messageUtil.get("image.delete.error")).thenReturn("Failed to delete image.");
        boolean result = imageService.deleteImageById(testUuid);

        assertTrue(result);
        assertFalse(Files.exists(filePath));
    }

    @Test
    void deleteImageById_fileDoesNotExist() {
        String nonExistingUuid = "non-existing-uuid.png";
        Path filePath = Paths.get(TEST_UPLOAD_DIR, nonExistingUuid);
        when(messageUtil.get("image.delete.error")).thenReturn("Failed to delete image.");
        boolean result = imageService.deleteImageById(nonExistingUuid);

        assertFalse(result);
        assertFalse(Files.exists(filePath));
    }


    // TODO: fix this mess
//    @Test
//    void uploadImageWithScaling_success() throws IOException {
//        MultipartFile mockImageFile = new MockMultipartFile("image", TEST_IMAGE_NAME, "image/png", TEST_IMAGE_DATA);
//        int targetWidth = 100;
//        int targetHeight = 100;
//        String fixedUuid = "a1b2c3d4-e5f6-7890-1234-567890abcdef";
//        String expectedImageId = fixedUuid + ".png";
//        String expectedImagePath = "/images/" + expectedImageId;
//        Path expectedPath = Paths.get(TEST_UPLOAD_DIR, expectedImageId);
//        BufferedImage mockOriginalImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
//        BufferedImage mockResizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
//        Image mockSavedImage = Image.builder().imageId(expectedImageId).build();
//        ImageUploadResponseDto expectedResponseDto = ImageUploadResponseDto.builder()
//                .imageId(expectedImageId)
//                .imagePath(expectedImagePath)
//                .build();
//
//        when(messageUtil.get("image.upload.error")).thenReturn("Image upload failed.");
//        when(messageUtil.get("image.upload.write.error")).thenReturn("Failed to write resized image.");
//        when(imageService.createImageUploadResponseDto(any(Image.class))).thenReturn(expectedResponseDto);
//        when(imageRepository.save(any(Image.class))).thenReturn(mockSavedImage);
//
//        ImageUploadResponseDto actualResponseDto;
//
//        try (var mockStatic = mockStatic(UUID.class);
//             var mockImageIOStatic = mockStatic(ImageIO.class)) {
//            mockStatic.when(UUID::randomUUID).thenReturn(UUID.fromString(fixedUuid));
//            mockImageIOStatic.when(() -> ImageIO.read(any(java.io.InputStream.class))).thenReturn(mockOriginalImage);
//            mockImageIOStatic.when(() -> ImageIO.write(eq(mockResizedImage), eq("png"), eq(expectedPath.toFile()))).thenReturn(true);
//
//            // Act
//            actualResponseDto = imageService.uploadImageWithScaling(mockImageFile, targetWidth, targetHeight);
//        }
//
//        assertEquals(expectedResponseDto, actualResponseDto);
//        verify(imageService, times(1)).validateImage(mockImageFile);
//        verify(imageService, times(1)).resizeImage(mockOriginalImage, targetWidth, targetHeight);
//        verify(imageRepository, times(1)).save(imageArgumentCaptor.capture());
//        Image savedImage = imageArgumentCaptor.getValue();
//        assertEquals(expectedImageId, savedImage.getImageId());
//        verify(imageService, times(1)).createImageUploadResponseDto(savedImage);
//        assertTrue(Files.exists(expectedPath));
//        Files.deleteIfExists(expectedPath);
//    }
}
