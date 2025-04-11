package com.spaghetticodegang.trylater.recommendation;

import com.spaghetticodegang.trylater.contact.ContactService;
import com.spaghetticodegang.trylater.recommendation.assignment.RecommendationAssignmentService;
import com.spaghetticodegang.trylater.recommendation.category.Category;
import com.spaghetticodegang.trylater.recommendation.category.CategoryRepository;
import com.spaghetticodegang.trylater.recommendation.category.CategoryType;
import com.spaghetticodegang.trylater.recommendation.dto.RecommendationRequestDto;
import com.spaghetticodegang.trylater.recommendation.dto.RecommendationResponseDto;
import com.spaghetticodegang.trylater.recommendation.tag.Tag;
import com.spaghetticodegang.trylater.recommendation.tag.group.TagGroup;
import com.spaghetticodegang.trylater.recommendation.tag.TagService;
import com.spaghetticodegang.trylater.shared.exception.ValidationException;
import com.spaghetticodegang.trylater.shared.util.MessageUtil;
import com.spaghetticodegang.trylater.user.User;
import com.spaghetticodegang.trylater.user.UserService;
import com.spaghetticodegang.trylater.user.dto.UserResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.net.URL;
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
    private MessageUtil messageUtil;

    @InjectMocks
    private RecommendationService recommendationService;

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
                .url(createUrl("https://example.com"))
                .rating(2)
                .category(categoryType)
                .tagIds(tagIds)
                .receiverIds(receiverIds)
                .build();
    }

    private URL createUrl(String value) {
        try {
            return new URI(value).toURL();
        } catch (Exception e) {
            return null;
        }
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
}
