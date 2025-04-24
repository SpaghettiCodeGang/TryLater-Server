package com.spaghetticodegang.trylater.image;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ImageTest {

    @Test
    void testBuilderAndGetterSetter() {
        String imgPath = "testImgPath";

        Image image = Image.builder()
                .imgPath(imgPath)
                .build();

        assertNotNull(image);
        assertEquals(imgPath, image.getImgPath());

        assertEquals(imgPath, image.getImgPath());
    }

    @Test
    void testNoArgsConstructor() {
        Image image = new Image();

        assertNotNull(image);
        assertNull(image.getImgPath());
    }

    @Test
    void testAllArgsConstructor() {
        String imgPath = "fullArgsConstructorId";

        Image image = new Image(imgPath);

        assertNotNull(image);
        assertEquals(imgPath, image.getImgPath());
    }

    @Test
    void testBuilderWithNoArgs() {
        Image image = Image.builder().build();

        assertNotNull(image);
        assertNull(image.getImgPath());
    }
}