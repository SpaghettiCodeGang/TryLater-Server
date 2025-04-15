package com.spaghetticodegang.trylater.recommendation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spaghetticodegang.trylater.recommendation.assignment.RecommendationAssignmentStatus;
import com.spaghetticodegang.trylater.recommendation.assignment.dto.RecommendationAssignmentStatusRequestDto;
import com.spaghetticodegang.trylater.recommendation.dto.RecommendationRequestDto;
import com.spaghetticodegang.trylater.recommendation.dto.RecommendationResponseDto;
import com.spaghetticodegang.trylater.recommendation.tag.dto.TagGroupResponseDto;
import com.spaghetticodegang.trylater.recommendation.category.CategoryType;
import com.spaghetticodegang.trylater.recommendation.tag.dto.TagResponseDto;
import com.spaghetticodegang.trylater.shared.util.MessageUtil;
import com.spaghetticodegang.trylater.user.User;
import com.spaghetticodegang.trylater.user.dto.UserResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecommendationController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecommendationService recommendationService;

    @MockBean
    private MessageUtil messageUtil;

    @Autowired
    private ObjectMapper objectMapper;

    public static User createMockUser() {
        return User.builder()
                .id(1L)
                .userName("tester")
                .displayName("tester")
                .imgPath("/assets/user.webp")
                .email("tester@example.com")
                .build();
    }

    public static UserResponseDto createCreatorDto() {
        return UserResponseDto.builder()
                .id(1L)
                .userName("tester")
                .displayName("tester")
                .imgPath("/assets/user.webp")
                .build();
    }

    public static RecommendationResponseDto createRecommendationResponse() {
        return RecommendationResponseDto.builder()
                .id(1L)
                .title("recommendation")
                .description("description")
                .imgPath("/assets/img.webp")
                .url("https://www.example.com")
                .rating(2)
                .creator(createCreatorDto())
                .creationDate(LocalDateTime.now())
                .category(CategoryType.MEDIA)
                .tagGroups(List.of(
                        TagGroupResponseDto.builder()
                                .tagGroupName("Genre")
                                .tags(List.of(TagResponseDto.builder()
                                        .id(1L)
                                        .tagName("Action")
                                        .build()))
                                .build()
                ))
                .build();
    }

    public static RecommendationRequestDto createRecommendationRequest() {
        return RecommendationRequestDto.builder()
                .title("recommendation")
                .description("description")
                .imgPath("./assets/img.webp")
                .url("https://www.example.com")
                .rating(2)
                .category(CategoryType.MEDIA)
                .tagIds(List.of(10L))
                .receiverIds(List.of(1L))
                .build();
    }

    @BeforeEach
    void setup() {
        var auth = new UsernamePasswordAuthenticationToken(createMockUser(), null, List.of());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void shouldReturn201_whenRecommendationCreatedSuccessfully() throws Exception {
        RecommendationRequestDto requestDto = createRecommendationRequest();

        when(recommendationService.createRecommendation(any(User.class), any(RecommendationRequestDto.class)))
                .thenReturn(createRecommendationResponse());

        mockMvc.perform(post("/api/recommendation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("recommendation"))
                .andExpect(jsonPath("$.imgPath").value("/assets/img.webp"))
                .andExpect(jsonPath("$.creator.userName").value("tester"))
                .andExpect(jsonPath("$.tagGroups[0].tagGroupName").value("Genre"))
                .andExpect(jsonPath("$.tagGroups[0].tags[0].tagName").value("Action"));
    }

    @Test
    void shouldReturn200_whenRecommendationAssignmentStatusUpdatedSuccessfully() throws Exception {
        RecommendationAssignmentStatusRequestDto requestDto = new RecommendationAssignmentStatusRequestDto();
        requestDto.setRecommendationAssignmentStatus(RecommendationAssignmentStatus.ACCEPTED);

        when(recommendationService.updateRecommendationAssignmentStatus(any(User.class), any(Long.class) , any(RecommendationAssignmentStatusRequestDto.class)))
                .thenReturn(createRecommendationResponse());

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/recommendation/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("recommendation"))
                .andExpect(jsonPath("$.imgPath").value("/assets/img.webp"))
                .andExpect(jsonPath("$.creator.userName").value("tester"))
                .andExpect(jsonPath("$.tagGroups[0].tagGroupName").value("Genre"))
                .andExpect(jsonPath("$.tagGroups[0].tags[0].tagName").value("Action"));
    }
}
