package com.spaghetticodegang.trylater.user;

import com.spaghetticodegang.trylater.shared.exception.ValidationException;
import com.spaghetticodegang.trylater.shared.util.MessageUtil;
import com.spaghetticodegang.trylater.user.dto.UserMeRegistrationDto;
import com.spaghetticodegang.trylater.user.dto.UserMeResponseDto;
import com.spaghetticodegang.trylater.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service layer for handling user-related business logic.
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageUtil messageUtil;

    /**
     * Loads a user by username or email for authentication.
     *
     * @param input the username or email
     * @return the user details
     * @throws UsernameNotFoundException if no matching user is found
     */
    @Override
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
        return userRepository.findByEmailOrUserName(input, input)
                .orElseThrow(() -> new UsernameNotFoundException("auth.invalid.credentials"));
    }

    /**
     * Finds a user by their unique ID.
     *
     * @param userId the ID of the user
     * @return the user entity
     * @throws UsernameNotFoundException if the user is not found
     */
    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("user.not.found"));

    }

    /**
     * Creates a public-facing user DTO from a {@link User} entity.
     * Intended for returning limited user info (e.g. in contact lists).
     *
     * @param user the user entity
     * @return a public response DTO
     */
    public UserResponseDto createUserResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .displayName(user.getDisplayName())
                .imgPath(user.getImgPath())
                .build();
    }

    /**
     * Creates a detailed user DTO from a {@link User} entity,
     * including sensitive fields like email.
     * Intended for authenticated users accessing their own profile.
     *
     * @param user the user entity
     * @return a full response DTO for the currently authenticated user
     */
    public UserMeResponseDto createUserMeResponseDto(User user) {
        return UserMeResponseDto.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .displayName(user.getDisplayName())
                .imgPath(user.getImgPath())
                .email(user.getEmail())
                .build();
    }

    /**
     * Registers a new user if the provided email and username are not already in use.
     *
     * @param userMeRegistrationDto the user input data for registration
     * @return a DTO containing the created user's public information
     * @throws ValidationException if the email or username already exists
     */
    public UserMeResponseDto registerUser(UserMeRegistrationDto userMeRegistrationDto) {
        final Map<String, String> errors = new HashMap<>();

        if (userRepository.existsByEmail(userMeRegistrationDto.getEmail())) {
            errors.put("email", messageUtil.get("user.email.exists"));
        }

        if (userRepository.existsByUserName(userMeRegistrationDto.getUserName())) {
            errors.put("userName", messageUtil.get("user.username.exists"));
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        final User user = User.builder()
                .userName(userMeRegistrationDto.getUserName())
                .displayName(userMeRegistrationDto.getDisplayName())
                .email(userMeRegistrationDto.getEmail())
                .password(passwordEncoder.encode(userMeRegistrationDto.getPassword()))
                // TODO: Platzhalterbild einfügen
                .imgPath("/assets/user.webp")
                .registrationDate(LocalDateTime.now())
                .build();

        userRepository.save(user);
        return createUserMeResponseDto(user);
    }

}
