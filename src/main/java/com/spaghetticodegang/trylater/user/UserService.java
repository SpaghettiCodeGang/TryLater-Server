package com.spaghetticodegang.trylater.user;

import com.spaghetticodegang.trylater.contact.ContactRepository;
import com.spaghetticodegang.trylater.image.ImageService;
import com.spaghetticodegang.trylater.recommendation.RecommendationRepository;
import com.spaghetticodegang.trylater.recommendation.assignment.RecommendationAssignment;
import com.spaghetticodegang.trylater.recommendation.assignment.RecommendationAssignmentRepository;
import com.spaghetticodegang.trylater.shared.exception.PasswordErrorException;
import com.spaghetticodegang.trylater.shared.exception.ValidationException;
import com.spaghetticodegang.trylater.shared.util.MessageUtil;
import com.spaghetticodegang.trylater.user.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service layer for handling user-related business logic.
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ImageService imageService;
    private final PasswordEncoder passwordEncoder;
    private final MessageUtil messageUtil;
    private final ContactRepository contactRepository;
    private final RecommendationAssignmentRepository recommendationAssignmentRepository;
    private final RecommendationRepository recommendationRepository;

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
                .displayName(userMeRegistrationDto.getUserName())
                .email(userMeRegistrationDto.getEmail())
                .password(passwordEncoder.encode(userMeRegistrationDto.getPassword()))
                .imgPath(null)
                .registrationDate(LocalDateTime.now())
                .build();

        userRepository.save(user);
        return createUserMeResponseDto(user);
    }

    /**
     * searches for user by their userName or email
     *
     * @param username the userName or email to search for
     * @return a DTO containing the user's public information
     * @throws UsernameNotFoundException if no matching user is found
     */
    public UserResponseDto searchUser(String username) {
        User user = userRepository.findByEmailOrUserName(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("user.not.found"));

        return createUserResponseDto(user);
    }

    /**
     * Updates the user profile with the new given entries.
     * Authentication required for username, email and new password.
     *
     * @param me              user entity that profile should be updated
     * @param userMeUpdateDto request dto with the new data
     * @return a full response DTO for the currently authenticated user
     * @throws PasswordErrorException if password input for authentication is incorrect
     * @throws ValidationException    if username or email already exists
     */
    public UserMeResponseDto updateUserProfile(User me, UserMeUpdateDto userMeUpdateDto) {
        final Map<String, String> errors = new HashMap<>();

        final boolean wantsToChangeUsername = userMeUpdateDto.getUserName() != null && !userMeUpdateDto.getUserName().equals(me.getUserName());
        final boolean wantsToChangeEmail = userMeUpdateDto.getEmail() != null && !userMeUpdateDto.getEmail().equals(me.getEmail());
        final boolean wantsToChangePassword = userMeUpdateDto.getNewPassword() != null;

        final boolean isSensitiveChange = wantsToChangeUsername || wantsToChangeEmail || wantsToChangePassword;

        if (isSensitiveChange) {
            if (userMeUpdateDto.getCurrentPassword() == null) {
                throw new PasswordErrorException("update.password.notblank");
            }
            if (!passwordEncoder.matches(userMeUpdateDto.getCurrentPassword(), me.getPassword())) {
                throw new PasswordErrorException("auth.invalid.password");
            }
        }

        if (wantsToChangeUsername) {
            if (userRepository.existsByUserName(userMeUpdateDto.getUserName())) {
                errors.put("userName", messageUtil.get("user.username.exists"));
            }
            me.setUserName(userMeUpdateDto.getUserName());
        }

        if (wantsToChangeEmail) {
            if (userRepository.existsByEmail(userMeUpdateDto.getEmail())) {
                errors.put("email", messageUtil.get("user.email.exists"));
            }
            me.setEmail(userMeUpdateDto.getEmail());
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        if (wantsToChangePassword) {
            me.setPassword(passwordEncoder.encode(userMeUpdateDto.getNewPassword()));
        }

        if (userMeUpdateDto.getDisplayName() != null) {
            me.setDisplayName(userMeUpdateDto.getDisplayName());
        }

        if (userMeUpdateDto.getImgPath() != null) {
            String currentImagePath = findUserById(me.getId()).getImgPath();
            if (currentImagePath != null) {
                imageService.deleteImageByImgPath(currentImagePath);
            }
            me.setImgPath(userMeUpdateDto.getImgPath());
        }

        userRepository.save(me);

        return createUserMeResponseDto(me);
    }

    /**
     * Deletes a user profile and manages the handling for deleting the contacts and assignments for that user.
     * Additional handles the deleting of not assigned recommendations
     *
     * @param me              user that should delete
     * @param userMeDeleteDto the dto for the request
     */
    public void deleteUserProfile(User me, UserMeDeleteDto userMeDeleteDto) {
        if (!passwordEncoder.matches(userMeDeleteDto.getPassword(), me.getPassword())) {
            throw new PasswordErrorException("auth.invalid.password");
        }
        if (me.getImgPath() != null) {
            imageService.deleteImageByImgPath(me.getImgPath());
        }

        contactRepository.deleteContactsByUserId(me.getId());
        final List<RecommendationAssignment> allAssignments = recommendationAssignmentRepository.findAllRecommendationAssignmentByUserId(me.getId());

        recommendationAssignmentRepository.deleteRecommendationAssignmentsByUserId(me.getId());
        allAssignments.forEach(assignment -> {
            Long recommendationId = assignment.getRecommendation().getId();
            if (!recommendationAssignmentRepository.existsRecommendationAssignmentByRecommendationId(recommendationId)) {
                recommendationRepository.deleteById(recommendationId);
            }
        });

        recommendationRepository.updateCreatorToNull(me.getId());
        userRepository.delete(me);
    }
}
