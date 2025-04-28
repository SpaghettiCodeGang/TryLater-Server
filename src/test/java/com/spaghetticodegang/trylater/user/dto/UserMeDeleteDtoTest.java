package com.spaghetticodegang.trylater.user.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserMeDeleteDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldNotHaveViolations_whenPasswordIsValid() {
        UserMeDeleteDto dto = new UserMeDeleteDto();
        dto.setPassword("secure123");

        Set<ConstraintViolation<UserMeDeleteDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldHaveViolation_whenPasswordTooShort() {
        UserMeDeleteDto dto = new UserMeDeleteDto();
        dto.setPassword("123"); // zu kurz

        Set<ConstraintViolation<UserMeDeleteDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        ConstraintViolation<UserMeDeleteDto> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("password");
        assertThat(violation.getMessage()).isEqualTo("{user.password.size}");
    }

    @Test
    void shouldHaveViolation_whenPasswordIsNull() {
        UserMeDeleteDto dto = new UserMeDeleteDto();
        dto.setPassword(null);

        Set<ConstraintViolation<UserMeDeleteDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }
}
