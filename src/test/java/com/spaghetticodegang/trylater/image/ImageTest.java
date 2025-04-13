package com.spaghetticodegang.trylater.image;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ImageTest {

    @Test
    void testBuilderAndGetterSetter() {
        String imageId = "testImageId";

        Image image = Image.builder()
                .imageId(imageId)
                .build();

        assertNotNull(image);
        assertEquals(imageId, image.getImageId());

        assertEquals(imageId, image.getImageId());
    }

    @Test
    void testNoArgsConstructor() {
        Image image = new Image();

        assertNotNull(image);
        assertNull(image.getImageId());
    }

    @Test
    void testAllArgsConstructor() {
        String imageId = "fullArgsConstructorId";

        Image image = new Image(imageId);

        assertNotNull(image);
        assertEquals(imageId, image.getImageId());
    }

    @Test
    void testBuilderWithNoArgs() {
        Image image = Image.builder().build();

        assertNotNull(image);
        assertNull(image.getImageId());
    }
}