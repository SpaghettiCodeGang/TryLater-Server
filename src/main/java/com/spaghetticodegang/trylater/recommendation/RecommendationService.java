package com.spaghetticodegang.trylater.recommendation;

import com.spaghetticodegang.trylater.contact.ContactService;
import com.spaghetticodegang.trylater.image.ImageService;
import com.spaghetticodegang.trylater.recommendation.assignment.RecommendationAssignment;
import com.spaghetticodegang.trylater.recommendation.assignment.RecommendationAssignmentService;
import com.spaghetticodegang.trylater.recommendation.assignment.RecommendationAssignmentStatus;
import com.spaghetticodegang.trylater.recommendation.assignment.dto.RecommendationAssignmentStatusRequestDto;
import com.spaghetticodegang.trylater.recommendation.category.Category;
import com.spaghetticodegang.trylater.recommendation.category.CategoryRepository;
import com.spaghetticodegang.trylater.recommendation.dto.RecommendationRequestDto;
import com.spaghetticodegang.trylater.recommendation.dto.RecommendationResponseDto;
import com.spaghetticodegang.trylater.recommendation.tag.Tag;
import com.spaghetticodegang.trylater.recommendation.tag.TagService;
import com.spaghetticodegang.trylater.shared.exception.RecommendationNotFoundException;
import com.spaghetticodegang.trylater.shared.exception.ValidationException;
import com.spaghetticodegang.trylater.shared.util.MessageUtil;
import com.spaghetticodegang.trylater.user.User;
import com.spaghetticodegang.trylater.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service layer for handling business logic related to recommendations.
 */
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final CategoryRepository categoryRepository;
    private final RecommendationAssignmentService recommendationAssignmentService;
    private final TagService tagService;
    private final UserService userService;
    private final ContactService contactService;
    private final ImageService imageService;
    private final MessageUtil messageUtil;

    /**
     * Creates a new recommendation and sent it to a list of contacts.
     * Performs validation to prevent invalid categories.
     *
     * @param me      the authenticated user
     * @param request the recommendation request containing the receiver IDs
     * @return a response DTO representing the newly created recommendation
     * @throws ValidationException if the category is not found
     */
    public RecommendationResponseDto createRecommendation(User me, RecommendationRequestDto request) {
        final Category category = categoryRepository.findByCategoryType(request.getCategory())
                .orElseThrow(() -> new ValidationException(Map.of("category", messageUtil.get("recommendation.category.not.found"))));

        final List<Tag> tags = request.getTagIds().stream()
                .map(id -> validateTagsBelongToCategory(category, id))
                .toList();

        final List<User> receivers = request.getReceiverIds().stream()
                .map(id -> validateReceiver(me, id))
                .toList();

        final Recommendation recommendation = Recommendation.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .imgPath(request.getImgPath())
                .url(request.getUrl())
                .rating(request.getRating())
                .creator(me)
                .creationDate(LocalDateTime.now())
                .category(category)
                .tags(tags)
                .build();

        recommendationRepository.save(recommendation);

        receivers.forEach(receiver -> {
            recommendationAssignmentService.createRecommendationAssignment(recommendation, receiver);
        });

        return createRecommendationResponseDto(recommendation);
    }

    /**
     * Creates a response DTO from a {@link Recommendation} entity.
     *
     * @param recommendation the recommendation entity
     * @return a response DTO representing the recommendation
     */
    public RecommendationResponseDto createRecommendationResponseDto(Recommendation recommendation) {
        return RecommendationResponseDto.builder()
                .id(recommendation.getId())
                .title(recommendation.getTitle())
                .description(recommendation.getDescription())
                .imgPath(recommendation.getImgPath())
                .url(recommendation.getUrl())
                .rating(recommendation.getRating())
                .creator(userService.createUserResponseDto(recommendation.getCreator()))
                .creationDate(recommendation.getCreationDate())
                .category(recommendation.getCategory().getCategoryType())
                .tagGroups(tagService.createTagGroupResponseDtoFromTags(recommendation.getTags()))
                .build();
    }

    /**
     * Updates the status of a {@link RecommendationAssignment} for the currently authenticated user.
     *
     * @param me               the currently authenticated {@link User}
     * @param recommendationId the ID of the {@link Recommendation} to update the assignment status
     * @param request          the {@link RecommendationAssignmentStatusRequestDto} containing the new status
     * @return a response DTO representing the recommendation
     * @throws RecommendationNotFoundException if there is no recommendation with requested ID
     */
    public RecommendationResponseDto updateRecommendationAssignmentStatus(User me, Long recommendationId, RecommendationAssignmentStatusRequestDto request) {
        recommendationAssignmentService.updateRecommendationAssignmentStatus(me, recommendationId, request);
        Recommendation recommendation = getRecommendationById(recommendationId);

        return createRecommendationResponseDto(recommendation);
    }

    /**
     * Validates that a tag with the given ID belongs to the specified category.
     * The category defines the valid TagGroups, and each Tag must belong to a TagGroup of this category.
     *
     * @param category the preselected category
     * @param tagId    the ID of the tag to validate
     * @return the validated {@link Tag} object
     * @throws ValidationException if the tag is not associated with the category
     */
    private Tag validateTagsBelongToCategory(Category category, Long tagId) {
        if (!tagService.getTagById(tagId).getTagGroup().getCategory().equals(category)) {
            throw new ValidationException(Map.of("tags", messageUtil.get("recommendation.tags.invalid.for.category")));
        }
        return tagService.getTagById(tagId);
    }

    /**
     * Validates that the receiver is a valid contact of the sender.
     *
     * @param sender     the user who sends the recommendation
     * @param receiverId the ID of the receiver
     * @return the validated {@link User} object
     * @throws ValidationException if the receiver is not a valid contact
     */
    private User validateReceiver(User sender, Long receiverId) {
        if (!(contactService.existsByUserIds(sender.getId(), receiverId) || Objects.equals(receiverId, sender.getId()))) {
            throw new ValidationException(Map.of("receiver", messageUtil.get("recommendation.receiver.not.valid")));
        }
        return userService.findUserById(receiverId);
    }

    /**
     * Finds a recommendation by its unique ID
     *
     * @param recommendationId the ID of the recommendation
     * @return the recommendation entity
     * @throws RecommendationNotFoundException if the recommendation is not found
     */
    private Recommendation getRecommendationById(Long recommendationId) {
        return recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new RecommendationNotFoundException("recommendation.not.found"));
    }

    /**
     * Lists all assigned recommendation with a specific status for a given user.
     *
     * @param me                             the currently authenticated user
     * @param recommendationAssignmentStatus status of the assigment
     * @return a list of response DTOs representing the recommendations
     */
    public List<RecommendationResponseDto> getAllRecommendationsByUserAndRecommendationStatus(User me, RecommendationAssignmentStatus recommendationAssignmentStatus) {
        List<Recommendation> recommendations = recommendationAssignmentService.getAllRecommendationsByUserAndAssignmentStatus(me, recommendationAssignmentStatus);

        return recommendations.stream()
                .map(this::createRecommendationResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Deletes a recommendation assignment from given user and recommendation ID.
     * Deletes a recommendation at all if there is no recommendation assignment for that recommendation.
     *
     * @param me               the user that assignment should be deleted
     * @param recommendationId the recommendation ID that assignment should be deleted
     */
    public void deleteRecommendationAssignment(User me, Long recommendationId) {
        recommendationAssignmentService.deleteRecommendationAssignmentByRecommendationId(me.getId(), recommendationId);
        if (!recommendationAssignmentService.existsRecommendationInRecommendationAssignment(recommendationId)) {
            String imagePath = getRecommendationById(recommendationId).getImgPath();
            if (imagePath != null) {
                imageService.deleteImageByImgPath(imagePath);
            }
            recommendationRepository.deleteById(recommendationId);
        }
    }
}