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
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.dto.AuthorDto;
import ru.otus.hw.services.dto.BookCreateDto;
import ru.otus.hw.services.dto.BookDto;
import ru.otus.hw.services.dto.BookUpdateDto;
import ru.otus.hw.services.dto.GenreDto;

import java.util.List;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookRestController.class)
class BookRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    private AuthorDto authorDto;

    private GenreDto genreDto;

    private BookDto bookDto;

    @BeforeEach
    void setUp() {
        authorDto = new AuthorDto(1, "Author_1");
        genreDto = new GenreDto(1, "Genre_1");
        bookDto = new BookDto(1, "BookTitle_1", authorDto, List.of(genreDto));
    }

    @DisplayName("должен возвращать список книг")
    @Test
    void shouldReturnBooksList() throws Exception {
        given(bookService.findAll()).willReturn(List.of(bookDto));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("BookTitle_1"))
                .andExpect(jsonPath("$[0].author.fullName").value("Author_1"))
                .andExpect(jsonPath("$[0].genres[0].name").value("Genre_1"));
    }

    @DisplayName("должен возвращать книгу по id")
    @Test
    void shouldReturnBookById() throws Exception {
        given(bookService.findById(1L)).willReturn(bookDto);

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("BookTitle_1"));
    }

    @DisplayName("должен возвращать 404 при отсутствии книги")
    @Test
    void shouldReturn404WhenBookNotFound() throws Exception {
        given(bookService.findById(100L)).willThrow(new EntityNotFoundException("Book with id 100 not found"));

        mockMvc.perform(get("/api/books/100"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Book with id 100 not found"));
    }

    @DisplayName("должен создавать книгу и возвращать 201")
    @Test
    void shouldCreateBook() throws Exception {
        var createDto = new BookCreateDto("New Book", 1L, Set.of(1L, 2L));
        given(bookService.insert(createDto)).willReturn(bookDto);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(bookService).insert(createDto);
    }

    @DisplayName("должен обновлять книгу")
    @Test
    void shouldUpdateBook() throws Exception {
        var createDto = new BookCreateDto("Updated Book", 2L, Set.of(3L));
        given(bookService.update(new BookUpdateDto(1L, "Updated Book", 2L, Set.of(3L)))).willReturn(bookDto);

        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk());

        verify(bookService).update(new BookUpdateDto(1L, "Updated Book", 2L, Set.of(3L)));
    }

    @DisplayName("должен удалять книгу и возвращать 204")
    @Test
    void shouldDeleteBook() throws Exception {
        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent());

        verify(bookService).deleteById(1L);
    }

    @DisplayName("не должен создавать книгу с пустым названием")
    @Test
    void shouldNotCreateBookWithBlankTitle() throws Exception {
        var invalid = new BookCreateDto("", 1L, Set.of(1L));

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @DisplayName("не должен создавать книгу без жанров")
    @Test
    void shouldNotCreateBookWithoutGenres() throws Exception {
        var invalid = new BookCreateDto("Title", 1L, Set.of());

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.genreIds").exists());
    }
}
