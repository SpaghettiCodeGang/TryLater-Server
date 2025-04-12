package com.spaghetticodegang.trylater.recommendation.assignment;

import com.spaghetticodegang.trylater.recommendation.Recommendation;
import com.spaghetticodegang.trylater.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class RecommendationAssignmentServiceTest {

    @Mock
    private RecommendationAssignmentRepository recommendationAssignmentRepository;

    @InjectMocks
    private RecommendationAssignmentService recommendationAssignmentService;

    private Recommendation createRecommendation() {
        return Recommendation.builder()
                .id(1L)
                .title("recommendation")
                .build();
    }

    private User createReceiver() {
        return User.builder()
                .id(2L)
                .userName("receiver")
                .build();
    }

    @Test
    void shouldCreateRecommendationAssignmentSuccessfully() {
        Recommendation recommendation = createRecommendation();
        User receiver = createReceiver();

        recommendationAssignmentService.createRecommendationAssignment(recommendation, receiver);

        verify(recommendationAssignmentRepository, times(1)).save(argThat(assignment ->
                assignment.getRecommendation().equals(recommendation) &&
                        assignment.getReceiver().equals(receiver) &&
                        assignment.getRecommendationAssignmentStatus() == RecommendationAssignmentStatus.SENT &&
                        assignment.getSentAt() != null && assignment.getSentAt().isBefore(LocalDateTime.now().plusSeconds(1))
        ));
    }
}
