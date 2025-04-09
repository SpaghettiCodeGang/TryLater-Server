package com.spaghetticodegang.trylater.recommendation.taggroup;


import com.spaghetticodegang.trylater.recommendation.category.Category;
import com.spaghetticodegang.trylater.recommendation.category.CategoryRepository;
import com.spaghetticodegang.trylater.recommendation.category.CategoryType;
import com.spaghetticodegang.trylater.recommendation.tag.group.TagGroup;
import com.spaghetticodegang.trylater.recommendation.tag.group.TagGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TagGroupRepositoryTest {

    @Autowired
    private TagGroupRepository tagGroupRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category category;
    private TagGroup tagGroup;

    @BeforeEach
    void setup() {
        category = categoryRepository.save(Category.builder()
                .categoryType(CategoryType.MEDIA)
                .build());

        tagGroup = tagGroupRepository.save(TagGroup.builder()
                .tagGroupName("Genre")
                .category(category)
                .build());
    }

    @Test
    void shouldFindByTagGroupNameAndCategory() {
        Optional<TagGroup> found = tagGroupRepository.findByTagGroupNameAndCategory("Genre", category);

        assertThat(found).isPresent();
        assertThat(found.get().getTagGroupName()).isEqualTo("Genre");
        assertThat(found.get().getCategory()).isEqualTo(category);
    }

    @Test
    void shouldReturnEmpty_whenTagGroupNotFound() {
        Optional<TagGroup> result = tagGroupRepository.findByTagGroupNameAndCategory("DoesNotExist", category);

        assertThat(result).isEmpty();
    }
}
