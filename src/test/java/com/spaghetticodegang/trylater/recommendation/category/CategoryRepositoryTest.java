package com.spaghetticodegang.trylater.recommendation.category;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldFindCategoryByCategoryType() {
        Category category = Category.builder()
                .categoryType(CategoryType.MEDIA)
                .build();

        categoryRepository.save(category);

        Optional<Category> found = categoryRepository.findByCategoryType(CategoryType.MEDIA);

        assertThat(found).isPresent();
        assertThat(found.get().getCategoryType()).isEqualTo(CategoryType.MEDIA);
    }

    @Test
    void shouldReturnEmpty_whenCategoryTypeNotFound() {
        Optional<Category> result = categoryRepository.findByCategoryType(CategoryType.PRODUCT);
        assertThat(result).isEmpty();
    }
}