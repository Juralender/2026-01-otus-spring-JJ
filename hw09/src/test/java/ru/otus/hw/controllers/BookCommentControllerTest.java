package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.services.BookCommentService;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookCommentController.class)
class BookCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookCommentService bookCommentService;

    @DisplayName("должен добавлять комментарий и делать редирект на страницу книги")
    @Test
    void shouldCreateCommentAndRedirect() throws Exception {
        mockMvc.perform(post("/books/1/comments")
                        .param("text", "New comment"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"));

        verify(bookCommentService).insert("New comment", 1L);
    }

    @DisplayName("должен удалять комментарий и делать редирект на страницу книги")
    @Test
    void shouldDeleteCommentAndRedirect() throws Exception {
        mockMvc.perform(post("/books/1/comments/5/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"));

        verify(bookCommentService).deleteById(5L);
    }

    @DisplayName("не должен позволять удалять комментарий методом GET")
    @Test
    void shouldNotAllowDeleteViaGet() throws Exception {
        mockMvc.perform(get("/books/1/comments/5/delete"))
                .andExpect(status().isMethodNotAllowed());
    }
}
