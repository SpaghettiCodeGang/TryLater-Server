package com.spaghetticodegang.trylater.user;

import com.spaghetticodegang.trylater.image.ImageService;
import com.spaghetticodegang.trylater.shared.exception.PasswordErrorException;
import com.spaghetticodegang.trylater.shared.exception.ValidationException;
import com.spaghetticodegang.trylater.shared.util.MessageUtil;
import com.spaghetticodegang.trylater.user.dto.UserMeRegistrationDto;
import com.spaghetticodegang.trylater.user.dto.UserMeResponseDto;
import com.spaghetticodegang.trylater.user.dto.UserMeUpdateDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Mock
    private ImageService imageService;

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
        assertNull(result.getImgPath());
    }

    @Test
    void shouldReturnUserResponseDto_whenSearchTermMatches() {
        User user = User.builder()
                .id(1L)
                .userName("searchTest")
                .displayName("Search Test")
                .email("search@example.com")
                .password("encodedPassword")
                .imgPath("/assets/user.webp")
                .registrationDate(LocalDateTime.now())
                .build();

        when(userRepository.findByEmailOrUserName("searchTest", "searchTest"))
                .thenReturn(java.util.Optional.of(user));

        var result = userService.searchUser("searchTest");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("searchTest", result.getUserName());
        assertEquals("Search Test", result.getDisplayName());
        assertEquals("/assets/user.webp", result.getImgPath());
    }

    @Test
    void shouldThrowException_whenSearchTermDoesNotMatch() {
        when(userRepository.findByEmailOrUserName("unknown", "unknown"))
                .thenReturn(java.util.Optional.empty());

        var ex = assertThrows(UsernameNotFoundException.class, () -> {
            userService.searchUser("unknown");
        });

        assertEquals("user.not.found", ex.getMessage());
    }

    @Test
    void shouldUpdateUserProfileSuccessfully_whenValidChangesProvided() {
        User user = User.builder()
                .id(1L)
                .userName("tester")
                .email("test@example.com")
                .password("encodedCurrentPass")
                .build();

        UserMeUpdateDto dto = new UserMeUpdateDto();
        dto.setUserName("newTester");
        dto.setEmail("new@example.com");
        dto.setCurrentPassword("currentPass123");
        dto.setNewPassword("newPass456");

        when(passwordEncoder.matches("currentPass123", "encodedCurrentPass")).thenReturn(true);
        when(userRepository.existsByUserName("newTester")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("newPass456")).thenReturn("encodedNewPass");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserMeResponseDto response = userService.updateUserProfile(user, dto);

        assertEquals("newTester", user.getUserName());
        assertEquals("new@example.com", user.getEmail());
        assertEquals("encodedNewPass", user.getPassword());

        assertEquals("newTester", response.getUserName());
    }

    @Test
    void shouldThrowPasswordErrorException_whenSensitiveChangeWithoutCurrentPassword() {
        User user = User.builder()
                .userName("tester")
                .email("test@example.com")
                .build();

        UserMeUpdateDto dto = new UserMeUpdateDto();
        dto.setUserName("changed");

        var ex = assertThrows(PasswordErrorException.class, () -> {
            userService.updateUserProfile(user, dto);
        });

        assertEquals("update.password.notblank", ex.getMessage());
    }

    @Test
    void shouldThrowPasswordErrorException_whenCurrentPasswordIsIncorrect() {
        User user = User.builder()
                .userName("tester")
                .email("test@example.com")
                .password("encodedPass")
                .build();

        UserMeUpdateDto dto = new UserMeUpdateDto();
        dto.setEmail("new@example.com");
        dto.setCurrentPassword("wrongPassword");

        when(passwordEncoder.matches("wrongPassword", "encodedPass")).thenReturn(false);

        var ex = assertThrows(PasswordErrorException.class, () -> {
            userService.updateUserProfile(user, dto);
        });

        assertEquals("auth.invalid.password", ex.getMessage());
    }

    @Test
    void shouldThrowValidationException_whenUsernameOrEmailExists() {
        User user = User.builder()
                .userName("tester")
                .email("test@example.com")
                .password("encodedPass")
                .build();

        UserMeUpdateDto dto = new UserMeUpdateDto();
        dto.setUserName("existingUser");
        dto.setEmail("existing@example.com");
        dto.setCurrentPassword("correctPassword");

        when(passwordEncoder.matches("correctPassword", "encodedPass")).thenReturn(true);
        when(userRepository.existsByUserName("existingUser")).thenReturn(true);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
        when(messageUtil.get("user.username.exists")).thenReturn("Benutzername existiert bereits.");
        when(messageUtil.get("user.email.exists")).thenReturn("E-Mail existiert bereits.");

        var ex = assertThrows(ValidationException.class, () -> {
            userService.updateUserProfile(user, dto);
        });

        assertTrue(ex.getErrors().containsKey("userName"));
        assertTrue(ex.getErrors().containsKey("email"));
        assertEquals("Benutzername existiert bereits.", ex.getErrors().get("userName"));
        assertEquals("E-Mail existiert bereits.", ex.getErrors().get("email"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldUpdateProfileWithoutPassword_whenOnlyDisplayNameChanges() {
        User user = User.builder()
                .userName("tester")
                .email("test@example.com")
                .password("encodedPass")
                .build();

        UserMeUpdateDto dto = new UserMeUpdateDto();
        dto.setDisplayName("Cooler Name");

        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserMeResponseDto response = userService.updateUserProfile(user, dto);

        assertEquals("Cooler Name", user.getDisplayName());
        assertEquals("Cooler Name", response.getDisplayName());
    }

    @Test
    void shouldUpdateProfileWithoutPassword_whenOnlyImgPathChanges() {
        User user = User.builder()
                .id(69L)
                .userName("tester")
                .email("test@example.com")
                .password("encodedPass")
                .imgPath("/assets/old.webp")
                .build();

        UserMeUpdateDto dto = new UserMeUpdateDto();
        dto.setImgPath("/assets/cool.webp");

        when(userRepository.findById(69L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        when(imageService.deleteImageByImgPath("/assets/old.webp")).thenReturn(true);

        UserMeResponseDto response = userService.updateUserProfile(user, dto);

        assertEquals("/assets/cool.webp", user.getImgPath());
        assertEquals("/assets/cool.webp", response.getImgPath());

        verify(imageService).deleteImageByImgPath("/assets/old.webp");
    }

    @Test
    void shouldUpdateProfileWithoutPassword_whenOnlyImgPathChangesAndCurrentImgPathIsNull() {
        User user = User.builder()
                .id(69L)
                .userName("tester")
                .email("test@example.com")
                .password("encodedPass")
                .build();

        UserMeUpdateDto dto = new UserMeUpdateDto();
        dto.setImgPath("/assets/cool.webp");

        when(userRepository.findById(69L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserMeResponseDto response = userService.updateUserProfile(user, dto);

        assertEquals("/assets/cool.webp", user.getImgPath());
        assertEquals("/assets/cool.webp", response.getImgPath());

        verify(imageService, never()).deleteImageByImgPath(anyString());
    }

}
