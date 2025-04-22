package com.spaghetticodegang.trylater.user.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserMeUpdateDtoTest {

    private static Validator validator;

    @BeforeAll
    public static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidDto() {
        UserMeUpdateDto dto = new UserMeUpdateDto();
        dto.setUserName("validUser");
        dto.setDisplayName("Cool User");
        dto.setEmail("user@example.com");
        dto.setCurrentPassword("secret123");
        dto.setNewPassword("newsecret");
        dto.setImgPath("/images/user.jpg");

        Set<ConstraintViolation<UserMeUpdateDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "DTO should be valid");
    }

    @Test
    public void testInvalidUserName() {
        UserMeUpdateDto dto = new UserMeUpdateDto();
        dto.setUserName("ab");
        dto.setEmail("user@example.com");
        dto.setCurrentPassword("secret123");
        dto.setNewPassword("newsecret");

        Set<ConstraintViolation<UserMeUpdateDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("userName")));
    }

    @Test
    public void testInvalidEmail() {
        UserMeUpdateDto dto = new UserMeUpdateDto();
        dto.setUserName("validUser");
        dto.setEmail("invalid-email");
        dto.setCurrentPassword("secret123");
        dto.setNewPassword("newsecret");

        Set<ConstraintViolation<UserMeUpdateDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    public void testInvalidPasswords() {
        UserMeUpdateDto dto = new UserMeUpdateDto();
        dto.setUserName("validUser");
        dto.setEmail("user@example.com");
        dto.setCurrentPassword("123");
        dto.setNewPassword("123");

        Set<ConstraintViolation<UserMeUpdateDto>> violations = validator.validate(dto);
        assertEquals(2, violations.size());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("currentPassword")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("newPassword")));
    }
}
