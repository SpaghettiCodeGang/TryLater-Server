package com.spaghetticodegang.trylater.recommendation.tag;

import com.spaghetticodegang.trylater.recommendation.category.CategoryType;
import com.spaghetticodegang.trylater.recommendation.tag.dto.TagGroupResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller providing endpoints for tag management.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendation/tags")
public class TagController {

    private final TagService tagService;

    /**
     * Retrieves all tags in tag groups that belong to the specified recommendation category.
     *
     * @param category the recommendation category to filter tags by {@link CategoryType}
     * @return a list of tags in tag groups relevant to the given category
     */
    @GetMapping()
    public ResponseEntity<List<TagGroupResponseDto>> getTagsByCategory(@RequestParam(name = "category", required = true) CategoryType category) {
        List<TagGroupResponseDto> result = tagService.getTagsByCategory(category);
        return ResponseEntity.ok(result);
    }

}