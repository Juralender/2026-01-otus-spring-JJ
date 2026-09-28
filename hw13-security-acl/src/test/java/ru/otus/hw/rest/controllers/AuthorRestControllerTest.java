package ru.otus.hw.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.dto.AuthorCreateDto;
import ru.otus.hw.services.dto.AuthorDto;
import ru.otus.hw.services.dto.AuthorUpdateDto;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthorRestController.class)
class AuthorRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthorService authorService;

    private AuthorDto authorDto;

    @BeforeEach
    void setUp() {
        authorDto = new AuthorDto(1, "Author_1");
    }

    @DisplayName("должен возвращать список авторов")
    @WithMockUser
    @Test
    void shouldReturnAuthorsList() throws Exception {
        given(authorService.findAll()).willReturn(List.of(authorDto));

        mockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("Author_1"));
    }

    @DisplayName("должен возвращать автора по id")
    @WithMockUser
    @Test
    void shouldReturnAuthorById() throws Exception {
        given(authorService.findById(1L)).willReturn(authorDto);

        mockMvc.perform(get("/api/authors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Author_1"));
    }

    @DisplayName("должен возвращать 404 при отсутствии автора")
    @WithMockUser
    @Test
    void shouldReturn404WhenAuthorNotFound() throws Exception {
        given(authorService.findById(100L)).willThrow(new EntityNotFoundException("Author with id 100 not found"));

        mockMvc.perform(get("/api/authors/100"))
                .andExpect(status().isNotFound());
    }

    @DisplayName("должен создавать автора и возвращать 201")
    @WithMockUser
    @Test
    void shouldCreateAuthor() throws Exception {
        var createDto = new AuthorCreateDto("New Author");
        given(authorService.insert(createDto)).willReturn(authorDto);

        mockMvc.perform(post("/api/authors")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated());

        verify(authorService).insert(createDto);
    }

    @DisplayName("должен обновлять автора")
    @WithMockUser
    @Test
    void shouldUpdateAuthor() throws Exception {
        var createDto = new AuthorCreateDto("Updated Author");
        given(authorService.update(new AuthorUpdateDto(1L, "Updated Author"))).willReturn(authorDto);

        mockMvc.perform(put("/api/authors/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk());

        verify(authorService).update(new AuthorUpdateDto(1L, "Updated Author"));
    }

    @DisplayName("не должен создавать автора с пустым именем")
    @WithMockUser
    @Test
    void shouldNotCreateAuthorWithBlankFullName() throws Exception {
        var invalid = new AuthorCreateDto("");

        mockMvc.perform(post("/api/authors")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fullName").exists());
    }

    @DisplayName("должен отклонять анонимный запрос списка авторов")
    @Test
    void shouldRejectAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/authors"))
                .andExpect(status().isUnauthorized());
    }
}
