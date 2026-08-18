package ru.otus.hw.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.controllers.dto.AuthorFormDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.dto.AuthorCreateDto;
import ru.otus.hw.services.dto.AuthorDto;
import ru.otus.hw.services.dto.AuthorUpdateDto;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AuthorController.class)
class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthorService authorService;

    private AuthorDto author;

    @BeforeEach
    void setUp() {
        author = new AuthorDto(1, "Author_1");
    }

    @DisplayName("должен отображать список авторов")
    @Test
    void shouldReturnAuthorsList() throws Exception {
        given(authorService.findAll()).willReturn(List.of(author));

        mockMvc.perform(get("/authors"))
                .andExpect(status().isOk())
                .andExpect(view().name("authors/list"))
                .andExpect(model().attribute("authors", List.of(author)));
    }

    @DisplayName("должен отображать форму создания автора")
    @Test
    void shouldReturnNewAuthorForm() throws Exception {
        mockMvc.perform(get("/authors/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("authors/form"))
                .andExpect(model().attribute("authorForm", new AuthorFormDto()));
    }

    @DisplayName("должен отображать форму редактирования, заполненную данными автора")
    @Test
    void shouldReturnEditAuthorForm() throws Exception {
        given(authorService.findById(1L)).willReturn(author);

        mockMvc.perform(get("/authors/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("authors/form"))
                .andExpect(model().attribute("authorForm", AuthorFormDto.fromDto(author)));
    }

    @DisplayName("должен возвращать 404 при редактировании несуществующего автора")
    @Test
    void shouldReturn404WhenEditingNonExistentAuthor() throws Exception {
        given(authorService.findById(100L)).willThrow(new EntityNotFoundException("Author with id 100 not found"));

        mockMvc.perform(get("/authors/100/edit"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"));
    }

    @DisplayName("должен создавать автора и делать редирект на список авторов")
    @Test
    void shouldCreateAuthorAndRedirect() throws Exception {
        mockMvc.perform(post("/authors")
                        .param("fullName", "New Author"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/authors"));

        verify(authorService).insert(new AuthorCreateDto("New Author"));
    }

    @DisplayName("должен обновлять автора и делать редирект на список авторов")
    @Test
    void shouldUpdateAuthorAndRedirect() throws Exception {
        mockMvc.perform(post("/authors/1")
                        .param("fullName", "Updated Author"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/authors"));

        verify(authorService).update(new AuthorUpdateDto(1L, "Updated Author"));
    }

    @DisplayName("не должен предоставлять способ удаления автора методом GET")
    @Test
    void shouldNotAllowDeleteViaGet() throws Exception {
        mockMvc.perform(get("/authors/1/delete"))
                .andExpect(status().isNotFound());
    }

    @DisplayName("не должен создавать автора с пустым именем")
    @Test
    void shouldNotCreateAuthorWithBlankFullName() throws Exception {
        mockMvc.perform(post("/authors")
                        .param("fullName", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("authors/form"))
                .andExpect(model().attributeHasFieldErrors("authorForm", "fullName"));
    }

    @DisplayName("не должен создавать автора с именем длиннее 25 символов")
    @Test
    void shouldNotCreateAuthorWithTooLongFullName() throws Exception {
        mockMvc.perform(post("/authors")
                        .param("fullName", "a".repeat(26)))
                .andExpect(status().isOk())
                .andExpect(view().name("authors/form"))
                .andExpect(model().attributeHasFieldErrors("authorForm", "fullName"));
    }
}
