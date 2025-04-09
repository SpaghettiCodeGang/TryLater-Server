package com.spaghetticodegang.trylater.recommendation.assignment;

import com.spaghetticodegang.trylater.recommendation.Recommendation;
import com.spaghetticodegang.trylater.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a recommendation assignment between recommendations and users.
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "recommendation_assignments")
public class RecommendationAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recommendation_id")
    private Recommendation recommendation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecommendationAssignmentStatus recommendationAssignmentStatus;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    private LocalDateTime acceptedAt;
}
