package com.spaghetticodegang.trylater.recommendation.tag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spaghetticodegang.trylater.recommendation.category.CategoryType;
import com.spaghetticodegang.trylater.recommendation.tag.dto.TagGroupResponseDto;
import com.spaghetticodegang.trylater.recommendation.tag.dto.TagResponseDto;
import com.spaghetticodegang.trylater.shared.util.MessageUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TagController.class)
@AutoConfigureMockMvc(addFilters = false)
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TagService tagService;

    @MockBean
    private MessageUtil messageUtil;

    @Autowired
    private ObjectMapper objectMapper;

    public static TagGroupResponseDto createMockTagGroup() {
        return TagGroupResponseDto.builder()
                .tagGroupName("Genre")
                .tags(List.of(
                        TagResponseDto.builder().id(1L).tagName("Action").build(),
                        TagResponseDto.builder().id(2L).tagName("Komödie").build()
                ))
                .build();
    }

    @Test
    void shouldReturn200WithTagGroups_whenValidCategoryProvided() throws Exception {
        when(tagService.getTagsByCategory(CategoryType.MEDIA))
                .thenReturn(List.of(createMockTagGroup()));

        mockMvc.perform(get("/api/recommendation/tags")
                        .param("category", "MEDIA"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].tagGroupName").value("Genre"))
                .andExpect(jsonPath("$[0].tags[0].tagName").value("Action"))
                .andExpect(jsonPath("$[0].tags[1].tagName").value("Komödie"));
    }

    @Test
    void shouldReturn400_whenCategoryIsMissing() throws Exception {
        when(messageUtil.get("recommendation.tag.category.required")).thenReturn("Kategorie ist erforderlich.");

        mockMvc.perform(get("/api/recommendation/tags"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400_whenCategoryIsInvalid() throws Exception {
        when(messageUtil.get("recommendation.tag.category.invalid")).thenReturn("Ungültige Kategorie.");

        mockMvc.perform(get("/api/recommendation/tags")
                        .param("category", "UNKNOWN"))
                .andExpect(status().isBadRequest());
    }
}
