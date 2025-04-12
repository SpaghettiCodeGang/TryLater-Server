package com.spaghetticodegang.trylater.user;

import com.spaghetticodegang.trylater.shared.exception.ValidationException;
import com.spaghetticodegang.trylater.shared.util.MessageUtil;
import com.spaghetticodegang.trylater.user.dto.UserMeRegistrationDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    void shouldLoadUserByUsernameOrEmail() {
        User user = User.builder()
                .email("test@example.com")
                .userName("tester")
                .password("secure123")
                .build();

        when(userRepository.findByEmailOrUserName("test@example.com", "test@example.com"))
                .thenReturn(java.util.Optional.of(user));

        var loaded = userService.loadUserByUsername("test@example.com");

        assertNotNull(loaded);
        assertEquals("test@example.com", loaded.getUsername()); // Spring Security's getUsername()
    }

    @Test
    void shouldFindUserById() {
        User user = User.builder().id(1L).userName("tester").build();
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));

        var result = userService.findUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void shouldThrowException_whenUserIdNotFound() {
        when(userRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        var ex = assertThrows(UsernameNotFoundException.class, () -> userService.findUserById(99L));
        assertEquals("user.not.found", ex.getMessage());
    }


    @Test
    void shouldThrowException_whenUserNotFound() {
        when(userRepository.findByEmailOrUserName("unknown", "unknown")).thenReturn(java.util.Optional.empty());

        var ex = assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("unknown");
        });

        assertEquals("auth.invalid.credentials", ex.getMessage());
    }

    @Test
    void shouldMapUserToUserResponseDto() {
        User user = User.builder()
                .id(2L)
                .userName("tester")
                .displayName("tester")
                .imgPath("/assets/user.webp")
                .build();

        var dto = userService.createUserResponseDto(user);

        assertEquals(2L, dto.getId());
        assertEquals("tester", dto.getUserName());
        assertEquals("tester", dto.getDisplayName());
        assertEquals("/assets/user.webp", dto.getImgPath());
    }

    @Test
    void shouldMapUserToUserMeResponseDto() {
        User user = User.builder()
                .id(1L)
                .userName("tester")
                .displayName("Tester")
                .email("test@example.com")
                .imgPath("/assets/user.webp")
                .registrationDate(LocalDateTime.now())
                .password("irrelevant")
                .build();

        var dto = userService.createUserMeResponseDto(user);

        assertEquals(1L, dto.getId());
        assertEquals("tester", dto.getUserName());
        assertEquals("Tester", dto.getDisplayName());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("/assets/user.webp", dto.getImgPath());
    }

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
