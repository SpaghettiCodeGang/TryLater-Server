package com.spaghetticodegang.trylater.recommendation.assignment;

import com.spaghetticodegang.trylater.recommendation.Recommendation;
import com.spaghetticodegang.trylater.recommendation.assignment.dto.RecommendationAssignmentStatusRequestDto;
import com.spaghetticodegang.trylater.shared.exception.RecommendationAssignmentNotFoundException;
import com.spaghetticodegang.trylater.shared.exception.ValidationException;
import com.spaghetticodegang.trylater.shared.util.MessageUtil;
import com.spaghetticodegang.trylater.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class RecommendationAssignmentServiceTest {

    @Mock
    private RecommendationAssignmentRepository recommendationAssignmentRepository;

    @InjectMocks
    private RecommendationAssignmentService recommendationAssignmentService;

    @Mock
    private MessageUtil messageUtil;

    private final Long recommendationAssignmentId = 123L;
    private final Long recommendationId = 436L;
    private final Long userId = 456L;
    private User authenticatedUser;
    private RecommendationAssignment existingAssignment;
    private RecommendationAssignmentStatusRequestDto requestDto;

    @BeforeEach
    void setUp() {
        authenticatedUser = new User();
        authenticatedUser.setId(userId);

        existingAssignment = new RecommendationAssignment();
        existingAssignment.setId(recommendationAssignmentId);
        existingAssignment.setReceiver(authenticatedUser);
        existingAssignment.setRecommendationAssignmentStatus(RecommendationAssignmentStatus.SENT);

        requestDto = new RecommendationAssignmentStatusRequestDto();
    }

    private Recommendation createRecommendation() {
        return Recommendation.builder()
                .id(1L)
                .title("recommendation")
                .creator(authenticatedUser)
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

    @Test
    void shouldCreateRecommendationAssignmentSuccessfullyToYourself() {
        Recommendation recommendation = createRecommendation();

        recommendationAssignmentService.createRecommendationAssignment(recommendation, authenticatedUser);

        verify(recommendationAssignmentRepository, times(1)).save(argThat(assignment ->
                assignment.getRecommendation().equals(recommendation) &&
                        assignment.getReceiver().equals(authenticatedUser) &&
                        assignment.getRecommendationAssignmentStatus() == RecommendationAssignmentStatus.ACCEPTED &&
                        assignment.getSentAt() != null && assignment.getSentAt().isBefore(LocalDateTime.now().plusSeconds(1))
        ));
    }

    @Test
    void updateRecommendationAssignmentStatus_userNotAllowedToUpdate() {
        User differentUser = new User();
        differentUser.setId(999L);
        existingAssignment.setReceiver(differentUser);
        requestDto.setRecommendationAssignmentStatus(RecommendationAssignmentStatus.ACCEPTED);

        when(recommendationAssignmentRepository.findById(recommendationAssignmentId)).thenReturn(Optional.of(existingAssignment));
        when(messageUtil.get("recommendation.assignment.error.user.not.allowed")).thenReturn("User not allowed to update this assignment.");

        ValidationException exception = assertThrows(ValidationException.class, () ->
                recommendationAssignmentService.updateRecommendationAssignmentStatus(authenticatedUser, recommendationAssignmentId, requestDto)
        );

        assertEquals("User not allowed to update this assignment.", exception.getErrors().get("recommendationAssignment"));
        verify(recommendationAssignmentRepository, times(1)).findById(recommendationAssignmentId);
        verify(recommendationAssignmentRepository, never()).save(any());
    }

    @Test
    void updateRecommendationAssignmentStatus_cannotRevertToSentStatus() {
        requestDto.setRecommendationAssignmentStatus(RecommendationAssignmentStatus.SENT);

        when(recommendationAssignmentRepository.findById(recommendationAssignmentId)).thenReturn(Optional.of(existingAssignment));
        when(messageUtil.get("recommendation.assignment.error.status.revert.to.sent")).thenReturn("Cannot revert status to SENT.");

        ValidationException exception = assertThrows(ValidationException.class, () ->
                recommendationAssignmentService.updateRecommendationAssignmentStatus(authenticatedUser, recommendationAssignmentId, requestDto)
        );

        assertEquals("Cannot revert status to SENT.", exception.getErrors().get("recommendationAssignmentStatus"));
        verify(recommendationAssignmentRepository, times(1)).findById(recommendationAssignmentId);
        verify(recommendationAssignmentRepository, never()).save(any());
    }

    @Test
    void getRecommendationAssignmentById_assignmentFound() {
        when(recommendationAssignmentRepository.findById(recommendationAssignmentId)).thenReturn(Optional.of(existingAssignment));

        RecommendationAssignment foundAssignment = recommendationAssignmentService.getRecommendationAssignmentById(recommendationAssignmentId);

        assertEquals(existingAssignment, foundAssignment);
        verify(recommendationAssignmentRepository, times(1)).findById(recommendationAssignmentId);
    }

    @Test
    void getRecommendationAssignmentById_assignmentNotFound_throwsException() {
        when(recommendationAssignmentRepository.findById(recommendationAssignmentId)).thenReturn(Optional.empty());
        RecommendationAssignmentNotFoundException exception = assertThrows(RecommendationAssignmentNotFoundException.class, () ->
                recommendationAssignmentService.getRecommendationAssignmentById(recommendationAssignmentId)
        );

        assertEquals("recommendation.assignment.error.not.found", exception.getMessage());
        verify(recommendationAssignmentRepository, times(1)).findById(recommendationAssignmentId);
    }

    @Test
    void updateRecommendationAssignmentStatus_shouldSetAcceptedAtWhenStatusIsAccepted() {
        User authenticatedUser = User.builder().id(1L).build();
        Long recommendationAssignmentId = 42L;

        RecommendationAssignment existingAssignment = RecommendationAssignment.builder()
                .id(recommendationAssignmentId)
                .receiver(authenticatedUser)
                .recommendationAssignmentStatus(RecommendationAssignmentStatus.SENT)
                .build();

        RecommendationAssignmentStatusRequestDto requestDto = new RecommendationAssignmentStatusRequestDto();
        requestDto.setRecommendationAssignmentStatus(RecommendationAssignmentStatus.ACCEPTED);

        when(recommendationAssignmentRepository.findById(recommendationAssignmentId)).thenReturn(Optional.of(existingAssignment));
        when(recommendationAssignmentRepository.save(any(RecommendationAssignment.class))).thenReturn(existingAssignment);

        recommendationAssignmentService.updateRecommendationAssignmentStatus(authenticatedUser, recommendationId, requestDto);

        assertEquals(recommendationAssignmentId, updatedId);
        assertEquals(RecommendationAssignmentStatus.ACCEPTED, existingAssignment.getRecommendationAssignmentStatus());
        assertNotNull(existingAssignment.getAcceptedAt(), "acceptedAt should be set when status is ACCEPTED");

        ArgumentCaptor<RecommendationAssignment> argumentCaptor = ArgumentCaptor.forClass(RecommendationAssignment.class);
        verify(recommendationAssignmentRepository).save(argumentCaptor.capture());

        RecommendationAssignment savedAssignment = argumentCaptor.getValue();
        assertEquals(RecommendationAssignmentStatus.ACCEPTED, savedAssignment.getRecommendationAssignmentStatus());
        assertNotNull(savedAssignment.getAcceptedAt());

        verify(recommendationAssignmentRepository).findById(recommendationAssignmentId);
    }

    @Test
    void shouldReturnRecommendationsByUserAndStatus() {
        User user = User.builder().id(1L).build();
        RecommendationAssignmentStatus status = RecommendationAssignmentStatus.ACCEPTED;

        Recommendation recommendation = Recommendation.builder()
                .id(42L)
                .title("Test Recommendation")
                .build();

        when(recommendationAssignmentRepository.findRecommendationsByUserIdAndRecommendationAssignmentStatus(user.getId(), status))
                .thenReturn(List.of(recommendation));

        List<Recommendation> result = recommendationAssignmentService.getAllRecommendationsByUserAndAssignmentStatus(user, status);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(recommendation.getId(), result.getFirst().getId());
        assertEquals("Test Recommendation", result.getFirst().getTitle());

        verify(recommendationAssignmentRepository, times(1))
                .findRecommendationsByUserIdAndRecommendationAssignmentStatus(user.getId(), status);
    }

    @Test
    void shouldReturnEmptyListIfNoRecommendationsFound() {
        User user = User.builder().id(99L).build();
        RecommendationAssignmentStatus status = RecommendationAssignmentStatus.SENT;

        when(recommendationAssignmentRepository.findRecommendationsByUserIdAndRecommendationAssignmentStatus(user.getId(), status))
                .thenReturn(List.of());

        List<Recommendation> result = recommendationAssignmentService.getAllRecommendationsByUserAndAssignmentStatus(user, status);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(recommendationAssignmentRepository, times(1))
                .findRecommendationsByUserIdAndRecommendationAssignmentStatus(user.getId(), status);
    }
    
    @Test
    void getRecommendationAssignmentByUserIdAndRecommendationId_assignmentFound() {
        when(recommendationAssignmentRepository.findRecommendationAssignmentByUserIdAndRecommendationId(userId, recommendationAssignmentId))
                .thenReturn(existingAssignment);

        RecommendationAssignment foundAssignment = recommendationAssignmentService.getRecommendationAssignmentByUserIdAndRecommendationId(userId, recommendationAssignmentId);

        assertEquals(existingAssignment, foundAssignment);
        verify(recommendationAssignmentRepository, times(1))
                .findRecommendationAssignmentByUserIdAndRecommendationId(userId, recommendationAssignmentId);
    }

    @Test
    void deleteRecommendationAssignmentByRecommendationId_assignmentNotFound_throwsException() {
        when(recommendationAssignmentService.getRecommendationAssignmentByUserIdAndRecommendationId(userId, recommendationId))
                .thenReturn(null);

        RecommendationAssignmentNotFoundException exception = assertThrows(RecommendationAssignmentNotFoundException.class, () ->
                recommendationAssignmentService.deleteRecommendationAssignmentByRecommendationId(userId, recommendationId)
        );

        assertEquals("recommendation.assignment.error.not.found", exception.getMessage());

        verify(recommendationAssignmentRepository, never())
                .deleteById(any());
    }

    @Test
    void deleteRecommendationAssignmentByRecommendationId_success() {
        when(recommendationAssignmentRepository.findRecommendationAssignmentByUserIdAndRecommendationId(userId, recommendationAssignmentId))
                .thenReturn(existingAssignment);

        recommendationAssignmentService.deleteRecommendationAssignmentByRecommendationId(userId, recommendationAssignmentId);

        verify(recommendationAssignmentRepository, times(1)).deleteById(existingAssignment.getId());
    }

    @Test
    void deleteRecommendationAssignmentByRecommendationId_notFound_throwsException() {
        when(recommendationAssignmentRepository.findRecommendationAssignmentByUserIdAndRecommendationId(userId, recommendationAssignmentId))
                .thenReturn(null);

        RecommendationAssignmentNotFoundException exception = assertThrows(RecommendationAssignmentNotFoundException.class, () ->
                recommendationAssignmentService.deleteRecommendationAssignmentByRecommendationId(userId, recommendationAssignmentId)
        );

        assertEquals("recommendation.assignment.error.not.found", exception.getMessage());
        verify(recommendationAssignmentRepository, times(1))
                .findRecommendationAssignmentByUserIdAndRecommendationId(userId, recommendationAssignmentId);
        verify(recommendationAssignmentRepository, never()).deleteById(any());
    }

    @Test
    void existsRecommendationInRecommendationAssignment_exists() {
        when(recommendationAssignmentRepository.existsRecommendationAssignmentByRecommendationId(recommendationAssignmentId))
                .thenReturn(true);

        boolean exists = recommendationAssignmentService.existsRecommendationInRecommendationAssignment(recommendationAssignmentId);

        assertTrue(exists);
        verify(recommendationAssignmentRepository, times(1))
                .existsRecommendationAssignmentByRecommendationId(recommendationAssignmentId);
    }

    @Test
    void existsRecommendationInRecommendationAssignment_doesNotExist() {
        when(recommendationAssignmentRepository.existsRecommendationAssignmentByRecommendationId(recommendationAssignmentId))
                .thenReturn(false);

        boolean exists = recommendationAssignmentService.existsRecommendationInRecommendationAssignment(recommendationAssignmentId);

        assertFalse(exists);
        verify(recommendationAssignmentRepository, times(1))
                .existsRecommendationAssignmentByRecommendationId(recommendationAssignmentId);
    }

}