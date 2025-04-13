package com.spaghetticodegang.trylater.recommendation.tag;

import com.spaghetticodegang.trylater.recommendation.category.Category;
import com.spaghetticodegang.trylater.recommendation.category.CategoryRepository;
import com.spaghetticodegang.trylater.recommendation.category.CategoryType;
import com.spaghetticodegang.trylater.recommendation.tag.group.TagGroup;
import com.spaghetticodegang.trylater.recommendation.tag.group.TagGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TagGroupRepository tagGroupRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category category;
    private TagGroup tagGroup;
    private Tag tag;

    @BeforeEach
    void setup() {
        category = categoryRepository.save(Category.builder()
                .categoryType(CategoryType.MEDIA)
                .build());

        tagGroup = tagGroupRepository.save(TagGroup.builder()
                .tagGroupName("Genre")
                .category(category)
                .build());

        tag = tagRepository.save(Tag.builder()
                .tagGroup(tagGroup)
                .tagName("Komödie")
                .build());
    }

    @Test
    void shouldReturnTrue_whenTagExistsInTagGroup() {
        boolean exists = tagRepository.existsByTagNameAndTagGroup("Komödie", tagGroup);

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalse_whenTagDoesNotExist() {
        boolean exists = tagRepository.existsByTagNameAndTagGroup("Action", tagGroup);

        assertThat(exists).isFalse();
    }
}