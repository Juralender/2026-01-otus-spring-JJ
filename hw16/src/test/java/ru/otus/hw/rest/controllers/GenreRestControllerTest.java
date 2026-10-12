package ru.otus.hw.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.GenreService;
import ru.otus.hw.services.dto.GenreCreateDto;
import ru.otus.hw.services.dto.GenreDto;
import ru.otus.hw.services.dto.GenreUpdateDto;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GenreRestController.class)
class GenreRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GenreService genreService;

    private GenreDto genreDto;

    @BeforeEach
    void setUp() {
        genreDto = new GenreDto(1, "Genre_1");
    }

    @DisplayName("должен возвращать список жанров")
    @Test
    void shouldReturnGenresList() throws Exception {
        given(genreService.findAll()).willReturn(List.of(genreDto));

        mockMvc.perform(get("/api/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Genre_1"));
    }

    @DisplayName("должен возвращать жанр по id")
    @Test
    void shouldReturnGenreById() throws Exception {
        given(genreService.findById(1L)).willReturn(genreDto);

        mockMvc.perform(get("/api/genres/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Genre_1"));
    }

    @DisplayName("должен возвращать 404 при отсутствии жанра")
    @Test
    void shouldReturn404WhenGenreNotFound() throws Exception {
        given(genreService.findById(100L)).willThrow(new EntityNotFoundException("Genre with id 100 not found"));

        mockMvc.perform(get("/api/genres/100"))
                .andExpect(status().isNotFound());
    }

    @DisplayName("должен создавать жанр и возвращать 201")
    @Test
    void shouldCreateGenre() throws Exception {
        var createDto = new GenreCreateDto("New Genre");
        given(genreService.insert(createDto)).willReturn(genreDto);

        mockMvc.perform(post("/api/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated());

        verify(genreService).insert(createDto);
    }

    @DisplayName("должен обновлять жанр")
    @Test
    void shouldUpdateGenre() throws Exception {
        var createDto = new GenreCreateDto("Updated");
        given(genreService.update(new GenreUpdateDto(1L, "Updated"))).willReturn(genreDto);

        mockMvc.perform(put("/api/genres/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk());

        verify(genreService).update(new GenreUpdateDto(1L, "Updated"));
    }

    @DisplayName("не должен создавать жанр с названием длиннее 10 символов")
    @Test
    void shouldNotCreateGenreWithTooLongName() throws Exception {
        var invalid = new GenreCreateDto("a".repeat(11));

        mockMvc.perform(post("/api/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }
}
