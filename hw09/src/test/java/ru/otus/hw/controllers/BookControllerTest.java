package ru.otus.hw.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.controllers.dto.BookFormDto;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookCommentService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.GenreService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private BookCommentService bookCommentService;

    @MockitoBean
    private AuthorService authorService;

    @MockitoBean
    private GenreService genreService;

    private Author author;

    private Genre genre;

    private Book book;

    @BeforeEach
    void setUp() {
        author = new Author(1, "Author_1");
        genre = new Genre(1, "Genre_1");
        book = new Book(1, "BookTitle_1", author, List.of(genre));
    }

    @DisplayName("должен отображать список книг")
    @Test
    void shouldReturnBooksList() throws Exception {
        given(bookService.findAll()).willReturn(List.of(book));

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"))
                .andExpect(model().attribute("books", List.of(book)));
    }

    @DisplayName("должен отображать книгу по id вместе с комментариями")
    @Test
    void shouldReturnBookById() throws Exception {
        given(bookService.findById(1L)).willReturn(Optional.of(book));
        given(bookCommentService.findAllByBookId(1L)).willReturn(List.of(new BookComment(1, "Comment", book)));

        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/view"))
                .andExpect(model().attribute("book", book))
                .andExpect(model().attributeExists("comments", "newComment"));
    }

    @DisplayName("должен возвращать 404 при отсутствии книги")
    @Test
    void shouldReturn404WhenBookNotFound() throws Exception {
        given(bookService.findById(100L)).willReturn(Optional.empty());

        mockMvc.perform(get("/books/100"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"));
    }

    @DisplayName("должен отображать форму создания книги со списками авторов и жанров")
    @Test
    void shouldReturnNewBookForm() throws Exception {
        given(authorService.findAll()).willReturn(List.of(author));
        given(genreService.findAll()).willReturn(List.of(genre));

        mockMvc.perform(get("/books/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().attribute("bookForm", new BookFormDto()))
                .andExpect(model().attribute("authors", List.of(author)))
                .andExpect(model().attribute("genres", List.of(genre)));
    }

    @DisplayName("должен отображать форму редактирования, заполненную данными книги")
    @Test
    void shouldReturnEditBookForm() throws Exception {
        given(bookService.findById(1L)).willReturn(Optional.of(book));
        given(authorService.findAll()).willReturn(List.of(author));
        given(genreService.findAll()).willReturn(List.of(genre));

        mockMvc.perform(get("/books/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().attribute("bookForm", BookFormDto.fromBook(book)));
    }

    @DisplayName("должен возвращать 404 при редактировании несуществующей книги")
    @Test
    void shouldReturn404WhenEditingNonExistentBook() throws Exception {
        given(bookService.findById(100L)).willReturn(Optional.empty());

        mockMvc.perform(get("/books/100/edit"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"));
    }

    @DisplayName("должен создавать книгу и делать редирект на список книг")
    @Test
    void shouldCreateBookAndRedirect() throws Exception {
        mockMvc.perform(post("/books")
                        .param("title", "New Book")
                        .param("authorId", "1")
                        .param("genreIds", "1", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService).insert("New Book", 1L, Set.of(1L, 2L));
    }

    @DisplayName("должен обновлять книгу и делать редирект на список книг")
    @Test
    void shouldUpdateBookAndRedirect() throws Exception {
        mockMvc.perform(post("/books/1")
                        .param("title", "Updated Book")
                        .param("authorId", "2")
                        .param("genreIds", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService).update(1L, "Updated Book", 2L, Set.of(3L));
    }

    @DisplayName("должен удалять книгу и делать редирект на список книг")
    @Test
    void shouldDeleteBookAndRedirect() throws Exception {
        mockMvc.perform(post("/books/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService).deleteById(1L);
    }

    @DisplayName("не должен позволять удалять книгу методом GET")
    @Test
    void shouldNotAllowDeleteViaGet() throws Exception {
        mockMvc.perform(get("/books/1/delete"))
                .andExpect(status().isMethodNotAllowed());
    }

    @DisplayName("не должен создавать книгу с пустым названием")
    @Test
    void shouldNotCreateBookWithBlankTitle() throws Exception {
        given(authorService.findAll()).willReturn(List.of(author));
        given(genreService.findAll()).willReturn(List.of(genre));

        mockMvc.perform(post("/books")
                        .param("title", "")
                        .param("authorId", "1")
                        .param("genreIds", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().attributeHasFieldErrors("bookForm", "title"))
                .andExpect(model().attributeExists("authors", "genres"));
    }

    @DisplayName("не должен создавать книгу с названием длиннее 30 символов")
    @Test
    void shouldNotCreateBookWithTooLongTitle() throws Exception {
        given(authorService.findAll()).willReturn(List.of(author));
        given(genreService.findAll()).willReturn(List.of(genre));

        mockMvc.perform(post("/books")
                        .param("title", "a".repeat(31))
                        .param("authorId", "1")
                        .param("genreIds", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().attributeHasFieldErrors("bookForm", "title"))
                .andExpect(model().attributeExists("authors", "genres"));
    }
}
