package com.spaghetticodegang.trylater.image;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class ImageRepositoryTest {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testSaveAndFindById() {
        Image image = Image.builder()
                .imgPath("test-image-123")
                .build();

        Image savedImage = imageRepository.save(image);
        Optional<Image> foundImage = imageRepository.findById(savedImage.getImgPath());

        assertTrue(foundImage.isPresent());
        assertEquals(image.getImgPath(), foundImage.get().getImgPath());
    }

    @Test
    void testExistsById() {
        Image image = Image.builder()
                .imgPath("existing-image")
                .build();
        entityManager.persist(image);
        entityManager.flush();

        boolean exists = imageRepository.existsById("existing-image");
        boolean notExists = imageRepository.existsById("non-existing-image");

        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    void testDeleteById() {
        Image image = Image.builder()
                .imgPath("to-be-deleted")
                .build();
        entityManager.persist(image);
        entityManager.flush();

        imageRepository.deleteById("to-be-deleted");
        Optional<Image> deletedImage = imageRepository.findById("to-be-deleted");

        assertTrue(deletedImage.isEmpty());
    }

    @Test
    void testDelete() {
        Image image = Image.builder()
                .imgPath("another-delete")
                .build();
        entityManager.persist(image);
        entityManager.flush();

        imageRepository.delete(image);
        Optional<Image> deletedImage = imageRepository.findById("another-delete");

        assertTrue(deletedImage.isEmpty());
    }
}