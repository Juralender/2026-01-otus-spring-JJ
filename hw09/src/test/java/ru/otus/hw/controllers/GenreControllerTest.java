package ru.otus.hw.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.controllers.dto.GenreFormDto;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.GenreService;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(GenreController.class)
class GenreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GenreService genreService;

    private Genre genre;

    @BeforeEach
    void setUp() {
        genre = new Genre(1, "Genre_1");
    }

    @DisplayName("должен отображать список жанров")
    @Test
    void shouldReturnGenresList() throws Exception {
        given(genreService.findAll()).willReturn(List.of(genre));

        mockMvc.perform(get("/genres"))
                .andExpect(status().isOk())
                .andExpect(view().name("genres/list"))
                .andExpect(model().attribute("genres", List.of(genre)));
    }

    @DisplayName("должен отображать форму создания жанра")
    @Test
    void shouldReturnNewGenreForm() throws Exception {
        mockMvc.perform(get("/genres/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("genres/form"))
                .andExpect(model().attribute("genreForm", new GenreFormDto()));
    }

    @DisplayName("должен отображать форму редактирования, заполненную данными жанра")
    @Test
    void shouldReturnEditGenreForm() throws Exception {
        given(genreService.findById(1L)).willReturn(Optional.of(genre));

        mockMvc.perform(get("/genres/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("genres/form"))
                .andExpect(model().attribute("genreForm", GenreFormDto.fromGenre(genre)));
    }

    @DisplayName("должен возвращать 404 при редактировании несуществующего жанра")
    @Test
    void shouldReturn404WhenEditingNonExistentGenre() throws Exception {
        given(genreService.findById(100L)).willReturn(Optional.empty());

        mockMvc.perform(get("/genres/100/edit"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"));
    }

    @DisplayName("должен создавать жанр и делать редирект на список жанров")
    @Test
    void shouldCreateGenreAndRedirect() throws Exception {
        mockMvc.perform(post("/genres")
                        .param("name", "New Genre"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/genres"));

        verify(genreService).insert("New Genre");
    }

    @DisplayName("должен обновлять жанр и делать редирект на список жанров")
    @Test
    void shouldUpdateGenreAndRedirect() throws Exception {
        mockMvc.perform(post("/genres/1")
                        .param("name", "Updated Genre"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/genres"));

        verify(genreService).update(1L, "Updated Genre");
    }

    @DisplayName("не должен предоставлять способ удаления жанра методом GET")
    @Test
    void shouldNotAllowDeleteViaGet() throws Exception {
        mockMvc.perform(get("/genres/1/delete"))
                .andExpect(status().isNotFound());
    }

    @DisplayName("не должен создавать жанр с пустым названием")
    @Test
    void shouldNotCreateGenreWithBlankName() throws Exception {
        mockMvc.perform(post("/genres")
                        .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("genres/form"))
                .andExpect(model().attributeHasFieldErrors("genreForm", "name"));
    }

    @DisplayName("не должен создавать жанр с названием длиннее 10 символов")
    @Test
    void shouldNotCreateGenreWithTooLongName() throws Exception {
        mockMvc.perform(post("/genres")
                        .param("name", "a".repeat(11)))
                .andExpect(status().isOk())
                .andExpect(view().name("genres/form"))
                .andExpect(model().attributeHasFieldErrors("genreForm", "name"));
    }
}
