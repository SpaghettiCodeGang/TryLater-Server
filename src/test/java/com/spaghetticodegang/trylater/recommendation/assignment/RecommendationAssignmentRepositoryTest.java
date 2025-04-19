package com.spaghetticodegang.trylater.recommendation.assignment;

import com.spaghetticodegang.trylater.recommendation.Recommendation;
import com.spaghetticodegang.trylater.recommendation.category.Category;
import com.spaghetticodegang.trylater.recommendation.category.CategoryType;
import com.spaghetticodegang.trylater.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ExtendWith(SpringExtension.class)
class RecommendationAssignmentRepositoryTest {

    @Autowired
    private RecommendationAssignmentRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldFindRecommendationAssignmentByUserIdAndRecommendationId() {
        User receiver = createUser("receiver");
        User creator = createUser("creator");
        entityManager.persist(receiver);
        entityManager.persist(creator);

        Category category = new Category();
        category.setCategoryType(CategoryType.MEDIA);
        entityManager.persist(category);

        Recommendation recommendation = Recommendation.builder()
                .title("Title")
                .description("Desc")
                .rating(1)
                .category(category)
                .creator(creator)
                .creationDate(LocalDateTime.now())
                .build();
        entityManager.persist(recommendation);

        RecommendationAssignment assignment = RecommendationAssignment.builder()
                .receiver(receiver)
                .recommendation(recommendation)
                .recommendationAssignmentStatus(RecommendationAssignmentStatus.SENT)
                .sentAt(LocalDateTime.now())
                .build();
        entityManager.persist(assignment);
        entityManager.flush();
        entityManager.clear();

        RecommendationAssignment found = repository.findRecommendationAssignmentByUserIdAndRecommendationId(
                receiver.getId(), recommendation.getId());

        assertThat(found).isNotNull();
        assertThat(found.getReceiver().getId()).isEqualTo(receiver.getId());
        assertThat(found.getRecommendation().getId()).isEqualTo(recommendation.getId());
    }

    @Test
    void shouldReturnTrue_whenAssignmentExistsForRecommendationId() {
        User user = createUser("user");
        entityManager.persist(user);

        Category category = new Category();
        category.setCategoryType(CategoryType.MEDIA);
        entityManager.persist(category);

        Recommendation recommendation = Recommendation.builder()
                .title("Test")
                .description("Test desc")
                .rating(2)
                .category(category)
                .creator(user)
                .creationDate(LocalDateTime.now())
                .build();
        entityManager.persist(recommendation);

        RecommendationAssignment assignment = RecommendationAssignment.builder()
                .receiver(user)
                .recommendation(recommendation)
                .recommendationAssignmentStatus(RecommendationAssignmentStatus.SENT)
                .sentAt(LocalDateTime.now())
                .build();
        entityManager.persist(assignment);
        entityManager.flush();

        boolean exists = repository.existsRecommendationAssignmentByRecommendationId(recommendation.getId());

        assertThat(exists).isTrue();
    }

    private User createUser(String name) {
        User user = new User();
        user.setUserName(name);
        user.setEmail(name + "@example.com");
        user.setDisplayName(name);
        user.setPassword("password");
        user.setRegistrationDate(LocalDateTime.now());
        return user;
    }
}
