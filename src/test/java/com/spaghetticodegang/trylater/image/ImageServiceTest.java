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

    private String getimgPathFromUpload(MultipartFile file) {
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
            assertEquals(expectedImageName, responseDto.getImgPath());

            verify(imageRepository, times(1)).save(imageCaptor.capture());
            assertEquals(expectedImageName, imageCaptor.getValue().getImgPath());

            verify(messageUtil, never()).get(anyString());
            Path path = Paths.get(expectedImagePath);
            assertTrue(Files.exists(path));
            Files.deleteIfExists(path);
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
        assertFalse(Files.exists(testUploadDirPath.resolve(getimgPathFromUpload(mockImageFile))));
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
        assertFalse(Files.exists(testUploadDirPath.resolve(getimgPathFromUpload(mockImageFile))));
    }

    @Test
    void deleteImageById_fileDeletesSuccessfully() throws IOException {
        String testUuid = "test-uuid.png";
        Path filePath = Paths.get(TEST_UPLOAD_DIR, testUuid);
        Files.createFile(filePath);
        when(messageUtil.get("image.delete.error")).thenReturn("Failed to delete image.");
        boolean result = imageService.deleteImageByImgPath(testUuid);

        assertTrue(result);
        assertFalse(Files.exists(filePath));
    }

    @Test
    void deleteImageById_fileDoesNotExist() {
        String nonExistingUuid = "non-existing-uuid.png";
        Path filePath = Paths.get(TEST_UPLOAD_DIR, nonExistingUuid);
        when(messageUtil.get("image.delete.error")).thenReturn("Failed to delete image.");
        boolean result = imageService.deleteImageByImgPath(nonExistingUuid);

        assertFalse(result);
        assertFalse(Files.exists(filePath));
    }

    @Test
    void deleteImageById_shouldThrowException_whenIOExceptionOccurs(){
        String imgPath = "test-image.jpg";
        Path filePath = Paths.get(TEST_UPLOAD_DIR, imgPath);

        when(messageUtil.get("image.delete.error")).thenReturn("Failed to delete image.");

        try (MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.exists(filePath)).thenReturn(true);
            filesMockedStatic.when(() -> Files.deleteIfExists(filePath))
                    .thenThrow(new IOException("Access denied."));

            assertThrows(ImageHandleException.class, () -> {
                imageService.deleteImageByImgPath(imgPath);
            });
        }
    }
}