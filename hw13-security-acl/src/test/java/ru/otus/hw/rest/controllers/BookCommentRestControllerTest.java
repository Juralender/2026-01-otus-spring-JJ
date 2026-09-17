package ru.otus.hw.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.services.BookCommentService;
import ru.otus.hw.services.dto.BookCommentCreateDto;
import ru.otus.hw.services.dto.BookCommentDto;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookCommentRestController.class)
class BookCommentRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookCommentService bookCommentService;

    @DisplayName("должен возвращать список комментариев книги")
    @WithMockUser
    @Test
    void shouldReturnCommentsList() throws Exception {
        given(bookCommentService.findAllByBookId(1L))
                .willReturn(List.of(new BookCommentDto(1, "Comment", 1L, "user")));

        mockMvc.perform(get("/api/books/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text").value("Comment"));
    }

    @DisplayName("должен добавлять комментарий и возвращать 201")
    @WithMockUser
    @Test
    void shouldCreateComment() throws Exception {
        var requestBody = new BookCommentCreateDto("New comment", 0L);
        given(bookCommentService.insert(new BookCommentCreateDto("New comment", 1L)))
                .willReturn(new BookCommentDto(5, "New comment", 1L, "user"));

        mockMvc.perform(post("/api/books/1/comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("New comment"));

        verify(bookCommentService).insert(new BookCommentCreateDto("New comment", 1L));
    }

    @DisplayName("не должен добавлять пустой комментарий")
    @WithMockUser
    @Test
    void shouldNotCreateBlankComment() throws Exception {
        var requestBody = new BookCommentCreateDto("", 0L);

        mockMvc.perform(post("/api/books/1/comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.text").exists());
    }

    @DisplayName("должен удалять комментарий и возвращать 204")
    @WithMockUser
    @Test
    void shouldDeleteComment() throws Exception {
        mockMvc.perform(delete("/api/books/1/comments/5")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(bookCommentService).deleteById(5L);
    }

    @DisplayName("должен отклонять анонимный запрос списка комментариев")
    @Test
    void shouldRejectAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/books/1/comments"))
                .andExpect(status().isUnauthorized());
    }
}
