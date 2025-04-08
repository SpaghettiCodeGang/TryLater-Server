package com.spaghetticodegang.trylater.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spaghetticodegang.trylater.recommendation.category.Category;
import com.spaghetticodegang.trylater.recommendation.category.CategoryRepository;
import com.spaghetticodegang.trylater.recommendation.dto.SeedCategoryDto;
import com.spaghetticodegang.trylater.recommendation.dto.SeedTagGroupDto;
import com.spaghetticodegang.trylater.recommendation.tag.Tag;
import com.spaghetticodegang.trylater.recommendation.tag.TagRepository;
import com.spaghetticodegang.trylater.recommendation.taggroup.TagGroup;
import com.spaghetticodegang.trylater.recommendation.taggroup.TagGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
@RequiredArgsConstructor
public class SeedDataConfig {

    private final CategoryRepository categoryRepository;
    private final TagGroupRepository tagGroupRepository;
    private final TagRepository tagRepository;
    private final ObjectMapper objectMapper;

    @Value("classpath:seed-data/categories.json")
    private Resource seedData;

    @Bean
    public CommandLineRunner seedFromJson() {
        return args -> {
            SeedCategoryDto[] seedCategories = objectMapper.readValue(seedData.getInputStream(), SeedCategoryDto[].class);

            for (SeedCategoryDto seedCategoryDto : seedCategories) {
                Category category = categoryRepository.findByCategoryType(seedCategoryDto.getCategoryType())
                        .orElseGet(() -> categoryRepository.save(
                                Category.builder().categoryType(seedCategoryDto.getCategoryType()).build()
                        ));

                for (SeedTagGroupDto seedTagGroupDto : seedCategoryDto.getTagGroups()) {
                    TagGroup tagGroup = tagGroupRepository.findByTagGroupNameAndCategory(seedTagGroupDto.getTagGroupName(), category)
                            .orElseGet(() -> tagGroupRepository.save(
                                    TagGroup.builder()
                                            .tagGroupName(seedTagGroupDto.getTagGroupName())
                                            .category(category)
                                            .build()
                            ));

                    for (String tagName : seedTagGroupDto.getTags()) {
                        if (!tagRepository.existsByTagNameAndTagGroup(tagName, tagGroup)) {
                            tagRepository.save(Tag.builder()
                                    .tagName(tagName)
                                    .tagGroup(tagGroup)
                                    .build());
                        }
                    }
                }
            }

            System.out.println("JSON-Seed erfolgreich ausgeführt.");
        };
    }
}
