package com.spaghetticodegang.trylater.user;

import com.spaghetticodegang.trylater.shared.exception.ValidationException;
import com.spaghetticodegang.trylater.shared.util.MessageUtil;
import com.spaghetticodegang.trylater.user.dto.UserMeRegistrationDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MessageUtil messageUtil;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldThrowValidationException_whenEmailExists() {
        var dto = new UserMeRegistrationDto();
        dto.setEmail("test@example.com");
        dto.setUserName("tester");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        when(messageUtil.get("user.email.exists")).thenReturn("E-Mail ist bereits registriert.");

        ValidationException ex = assertThrows(ValidationException.class, () -> {
            userService.registerUser(dto);
        });

        assertTrue(ex.getErrors().containsKey("email"));
        assertEquals("E-Mail ist bereits registriert.", ex.getErrors().get("email"));
    }

    @Test
    void shouldThrowValidationException_whenUsernameExists() {
        var dto = new UserMeRegistrationDto();
        dto.setEmail("test@example.com");
        dto.setUserName("tester");

        when(userRepository.existsByUserName("tester")).thenReturn(true);
        when(messageUtil.get("user.username.exists")).thenReturn("Benutzername ist bereits vergeben.");

        ValidationException ex = assertThrows(ValidationException.class, () -> {
            userService.registerUser(dto);
        });

        assertTrue(ex.getErrors().containsKey("userName"));
        assertEquals("Benutzername ist bereits vergeben.", ex.getErrors().get("userName"));
    }

    @Test
    void shouldCreateUser_whenEmailAndUsernameAreAvailable() {
        var dto = new UserMeRegistrationDto();
        dto.setEmail("test@example.com");
        dto.setUserName("tester");
        dto.setDisplayName("tester");
        dto.setPassword("secure123");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByUserName("tester")).thenReturn(false);
        when(passwordEncoder.encode("secure123")).thenReturn("encodedPassword");

        User savedUser = User.builder()
                .id(1L)
                .userName("tester")
                .displayName("tester")
                .email("test@example.com")
                .password("encodedPassword")
                .imgPath("/assets/user.webp")
                .registrationDate(LocalDateTime.now())
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        var result = userService.registerUser(dto);

        assertNotNull(result);
        assertEquals("tester", result.getUserName());
        assertEquals("tester", result.getDisplayName());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("/assets/user.webp", result.getImgPath());
    }

}
