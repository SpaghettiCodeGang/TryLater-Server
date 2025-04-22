package com.spaghetticodegang.trylater.recommendation;

import com.spaghetticodegang.trylater.contact.ContactService;
import com.spaghetticodegang.trylater.image.ImageService;
import com.spaghetticodegang.trylater.recommendation.assignment.RecommendationAssignmentService;
import com.spaghetticodegang.trylater.recommendation.assignment.RecommendationAssignmentStatus;
import com.spaghetticodegang.trylater.recommendation.assignment.dto.RecommendationAssignmentStatusRequestDto;
import com.spaghetticodegang.trylater.recommendation.category.Category;
import com.spaghetticodegang.trylater.recommendation.category.CategoryRepository;
import com.spaghetticodegang.trylater.recommendation.category.CategoryType;
import com.spaghetticodegang.trylater.recommendation.dto.RecommendationRequestDto;
import com.spaghetticodegang.trylater.recommendation.dto.RecommendationResponseDto;
import com.spaghetticodegang.trylater.recommendation.tag.Tag;
import com.spaghetticodegang.trylater.recommendation.tag.group.TagGroup;
import com.spaghetticodegang.trylater.recommendation.tag.TagService;
import com.spaghetticodegang.trylater.shared.exception.RecommendationNotFoundException;
import com.spaghetticodegang.trylater.shared.exception.ValidationException;
import com.spaghetticodegang.trylater.shared.util.MessageUtil;
import com.spaghetticodegang.trylater.user.User;
import com.spaghetticodegang.trylater.user.UserService;
import com.spaghetticodegang.trylater.user.dto.UserResponseDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private RecommendationRepository recommendationRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private RecommendationAssignmentService recommendationAssignmentService;

    @Mock
    private TagService tagService;

    @Mock
    private UserService userService;

    @Mock
    private ContactService contactService;

    @Mock
    private ImageService imageService;

    @Mock
    private MessageUtil messageUtil;

    @InjectMocks
    private RecommendationService recommendationService;

    private final Long recommendationId = 69L;

    private User createUser(Long id) {
        return User.builder()
                .id(id)
                .userName("user" + id)
                .email("user" + id + "@example.com")
                .build();
    }

    private Category createCategory(CategoryType categoryType) {
        return Category.builder()
                .categoryType(categoryType)
                .build();
    }

    private Tag createTag(Long id, Category category) {
        return Tag.builder()
                .id(id)
                .tagGroup(TagGroup.builder()
                        .category(category)
                        .build())
                .build();
    }

    private RecommendationRequestDto createRequestDto(CategoryType categoryType, List<Long> tagIds, List<Long> receiverIds) {
        return RecommendationRequestDto.builder()
                .title("recommendation")
                .description("description")
                .imgPath("./assets/img.png")
                .url("https://www.example.com")
                .rating(2)
                .category(categoryType)
                .tagIds(tagIds)
                .receiverIds(receiverIds)
                .build();
    }

    @Test
    void shouldCreateRecommendationSuccessfully() {
        User creator = createUser(1L);
        User receiver = createUser(2L);
        Category category = createCategory(CategoryType.MEDIA);
        Tag tag = createTag(10L, category);
        RecommendationRequestDto request = createRequestDto(CategoryType.MEDIA, List.of(tag.getId()), List.of(receiver.getId()));

        when(categoryRepository.findByCategoryType(CategoryType.MEDIA)).thenReturn(Optional.of(category));
        when(tagService.getTagById(tag.getId())).thenReturn(tag);
        when(contactService.existsByUserIds(creator.getId(), receiver.getId())).thenReturn(true);
        when(userService.findUserById(receiver.getId())).thenReturn(receiver);
        when(userService.createUserResponseDto(creator)).thenReturn(UserResponseDto.builder().id(1L).build());
        when(tagService.createTagGroupResponseDtoFromTags(anyList())).thenReturn(List.of());

        RecommendationResponseDto result = recommendationService.createRecommendation(creator, request);

        assertNotNull(result);
        assertEquals("recommendation", result.getTitle());
        assertEquals("description", result.getDescription());
        assertEquals("./assets/img.png", result.getImgPath());
        assertEquals(2L, result.getRating());
        assertEquals(CategoryType.MEDIA, result.getCategory());
        verify(recommendationRepository).save(any(Recommendation.class));
        verify(recommendationAssignmentService).createRecommendationAssignment(any(Recommendation.class), eq(receiver));
    }

    @Test
    void shouldThrowValidationException_whenCategoryNotFound() {
        User creator = createUser(1L);
        RecommendationRequestDto request = createRequestDto(CategoryType.MEDIA, List.of(), List.of());

        when(categoryRepository.findByCategoryType(CategoryType.MEDIA)).thenReturn(Optional.empty());
        when(messageUtil.get("recommendation.category.not.found")).thenReturn("Kategorie nicht gefunden");

        ValidationException ex = assertThrows(ValidationException.class, () -> {
            recommendationService.createRecommendation(creator, request);
        });

        assertTrue(ex.getErrors().containsKey("category"));
        assertEquals("Kategorie nicht gefunden", ex.getErrors().get("category"));
    }

    @Test
    void shouldThrowValidationException_whenTagDoesNotMatchCategory() {
        User creator = createUser(1L);
        Category correctCategory = createCategory(CategoryType.MEDIA);
        Category wrongCategory = createCategory(CategoryType.LOCATION);
        Tag tag = createTag(10L, wrongCategory);
        RecommendationRequestDto request = createRequestDto(CategoryType.MEDIA, List.of(tag.getId()), List.of(createUser(2L).getId()));

        when(categoryRepository.findByCategoryType(CategoryType.MEDIA)).thenReturn(Optional.of(correctCategory));
        when(tagService.getTagById(tag.getId())).thenReturn(tag);
        when(messageUtil.get("recommendation.tags.invalid.for.category")).thenReturn("Die Tags sind für diese Kategorie nicht zulässig.");

        ValidationException ex = assertThrows(ValidationException.class, () -> {
            recommendationService.createRecommendation(creator, request);
        });

        assertTrue(ex.getErrors().containsKey("tags"));
        assertEquals("Die Tags sind für diese Kategorie nicht zulässig.", ex.getErrors().get("tags"));
    }

    @Test
    void shouldThrowValidationException_whenReceiverIsNotContact() {
        User creator = createUser(1L);
        User receiver = createUser(2L);
        Category category = createCategory(CategoryType.MEDIA);
        Tag tag = createTag(10L, category);
        RecommendationRequestDto request = createRequestDto(CategoryType.MEDIA, List.of(tag.getId()), List.of(receiver.getId()));

        when(categoryRepository.findByCategoryType(CategoryType.MEDIA)).thenReturn(Optional.of(category));
        when(tagService.getTagById(tag.getId())).thenReturn(tag);
        when(contactService.existsByUserIds(creator.getId(), receiver.getId())).thenReturn(false);
        when(messageUtil.get("recommendation.receiver.not.valid")).thenReturn("Der angegebene Empfänger ist kein gültiger Kontakt.");

        ValidationException ex = assertThrows(ValidationException.class, () -> {
            recommendationService.createRecommendation(creator, request);
        });

        assertTrue(ex.getErrors().containsKey("receiver"));
        assertEquals("Der angegebene Empfänger ist kein gültiger Kontakt.", ex.getErrors().get("receiver"));
    }

    @Test
    void testUpdateRecommendationAssignmentStatus_recommendationNotFound() {
        User me = new User();
        Long recommendationAssignmentId = 1L;
        Long recommendationId = 10L;
        RecommendationAssignmentStatusRequestDto requestDto = new RecommendationAssignmentStatusRequestDto();

        Mockito.when(recommendationAssignmentService.updateRecommendationAssignmentStatus(me, recommendationAssignmentId, requestDto))
                .thenReturn(recommendationId);
        Mockito.when(recommendationRepository.findById(recommendationId))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(RecommendationNotFoundException.class, () -> {
            recommendationService.updateRecommendationAssignmentStatus(me, recommendationAssignmentId, requestDto);
        });
    }

    @Test
    void shouldUpdateRecommendationAssignmentStatusSuccessfully() {
        User user = createUser(1L);
        Long recommendationAssignmentId = 100L;
        Long recommendationId = 200L;

        RecommendationAssignmentStatusRequestDto requestDto = new RecommendationAssignmentStatusRequestDto();
        requestDto.setRecommendationAssignmentStatus(RecommendationAssignmentStatus.ACCEPTED);

        Recommendation recommendation = Recommendation.builder()
                .id(recommendationId)
                .title("Title")
                .description("Description")
                .rating(3)
                .imgPath("path.png")
                .category(createCategory(CategoryType.MEDIA))
                .build();

        when(recommendationAssignmentService.updateRecommendationAssignmentStatus(user, recommendationAssignmentId, requestDto))
                .thenReturn(recommendationId);
        when(recommendationRepository.findById(recommendationId))
                .thenReturn(Optional.of(recommendation));

        RecommendationResponseDto response = recommendationService.updateRecommendationAssignmentStatus(user, recommendationAssignmentId, requestDto);

        assertNotNull(response);
        assertEquals(recommendation.getId(), response.getId());
        assertEquals(recommendation.getTitle(), response.getTitle());
        assertEquals(recommendation.getDescription(), response.getDescription());
        assertEquals(recommendation.getRating(), response.getRating());
        assertEquals(recommendation.getImgPath(), response.getImgPath());
        assertEquals(recommendation.getCategory().getCategoryType(), response.getCategory());

        verify(recommendationAssignmentService).updateRecommendationAssignmentStatus(user, recommendationAssignmentId, requestDto);
        verify(recommendationRepository).findById(recommendationId);
    }

    @Test
    void shouldReturnRecommendationsFilteredByStatus() {
        User user = createUser(1L);
        RecommendationAssignmentStatus status = RecommendationAssignmentStatus.ACCEPTED;

        Recommendation recommendation = Recommendation.builder()
                .id(1L)
                .title("Test Recommendation")
                .description("Some description")
                .rating(4)
                .imgPath("/img/test.png")
                .category(createCategory(CategoryType.MEDIA))
                .build();

        List<Recommendation> mockRecommendations = List.of(recommendation);

        when(recommendationAssignmentService.getAllRecommendationsByUserAndAssignmentStatus(user, status))
                .thenReturn(mockRecommendations);

        List<RecommendationResponseDto> responseList =
                recommendationService.getAllRecommendationsByUserAndRecommendationStatus(user, status);

        assertNotNull(responseList);
        assertEquals(1, responseList.size());
        RecommendationResponseDto responseDto = responseList.get(0);
        assertEquals(recommendation.getId(), responseDto.getId());
        assertEquals(recommendation.getTitle(), responseDto.getTitle());
        assertEquals(recommendation.getDescription(), responseDto.getDescription());
        assertEquals(recommendation.getRating(), responseDto.getRating());
        assertEquals(recommendation.getImgPath(), responseDto.getImgPath());
        assertEquals(recommendation.getCategory().getCategoryType(), responseDto.getCategory());

        verify(recommendationAssignmentService).getAllRecommendationsByUserAndAssignmentStatus(user, status);
    }

    @Test
    void deleteRecommendationAssignment_recommendationStillExists_doesNotDeleteRecommendation() {
        User user = createUser(1L);
        when(recommendationAssignmentService.existsRecommendationInRecommendationAssignment(recommendationId))
                .thenReturn(true);

        recommendationService.deleteRecommendationAssignment(user, recommendationId);

        verify(recommendationAssignmentService).deleteRecommendationAssignmentByRecommendationId(user.getId(), recommendationId);
        verify(recommendationAssignmentService).existsRecommendationInRecommendationAssignment(recommendationId);
        verify(recommendationRepository, never()).deleteById(any());
    }

    @Test
    void deleteRecommendationAssignment_recommendationNoLongerExists_deletesRecommendationWithImage() {
        Long recommendationId = 42L;
        User user = createUser(1L);

        Recommendation recommendation = new Recommendation();
        recommendation.setId(recommendationId);
        recommendation.setImgPath("some/image/path.jpg");

        when(recommendationAssignmentService.existsRecommendationInRecommendationAssignment(recommendationId))
                .thenReturn(false);
        when(recommendationRepository.findById(recommendationId))
                .thenReturn(Optional.of(recommendation));

        recommendationService.deleteRecommendationAssignment(user, recommendationId);

        verify(recommendationAssignmentService)
                .deleteRecommendationAssignmentByRecommendationId(user.getId(), recommendationId);
        verify(recommendationAssignmentService)
                .existsRecommendationInRecommendationAssignment(recommendationId);
        verify(recommendationRepository).findById(recommendationId);
        verify(recommendationRepository).deleteById(recommendationId);
        verify(imageService).deleteImageById("some/image/path.jpg");
    }

    @Test
    void deleteRecommendationAssignment_recommendationNoLongerExists_deletesRecommendationWithoutAnImage() {
        Long recommendationId = 42L;
        User user = createUser(1L);

        Recommendation recommendation = new Recommendation();
        recommendation.setId(recommendationId);
        recommendation.setImgPath(null);

        when(recommendationAssignmentService.existsRecommendationInRecommendationAssignment(recommendationId))
                .thenReturn(false);
        when(recommendationRepository.findById(recommendationId))
                .thenReturn(Optional.of(recommendation));

        recommendationService.deleteRecommendationAssignment(user, recommendationId);

        verify(recommendationAssignmentService)
                .deleteRecommendationAssignmentByRecommendationId(user.getId(), recommendationId);
        verify(recommendationAssignmentService)
                .existsRecommendationInRecommendationAssignment(recommendationId);
        verify(recommendationRepository).findById(recommendationId);
        verify(recommendationRepository).deleteById(recommendationId);
    }
}
