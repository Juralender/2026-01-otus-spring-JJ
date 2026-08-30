package ru.otus.hw.rest.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookGenreRelation;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookGenreRelationRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;
import ru.otus.hw.services.dto.BookCreateDto;
import ru.otus.hw.services.dto.BookUpdateDto;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@WebFluxTest(BookRestController.class)
class BookRestControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AuthorRepository authorRepository;

    @MockitoBean
    private GenreRepository genreRepository;

    @MockitoBean
    private BookRepository bookRepository;

    @MockitoBean
    private BookGenreRelationRepository bookGenreRelationRepository;

    private Author author;

    private Genre genre;

    private Book book;

    @BeforeEach
    void setUp() {
        author = new Author(1, "Author_1");
        genre = new Genre(1, "Genre_1");
        book = new Book(1, "BookTitle_1", 1L);

        given(authorRepository.findAllByIdIn(anyCollection())).willReturn(Flux.just(author));
        given(bookGenreRelationRepository.findAllByBookIdIn(anyCollection()))
                .willReturn(Flux.just(new BookGenreRelation(1L, 1L)));
        given(genreRepository.findAllByIdIn(anySet())).willReturn(Flux.just(genre));
    }

    @DisplayName("должен возвращать список книг")
    @Test
    void shouldReturnBooksList() {
        given(bookRepository.findAll()).willReturn(Flux.just(book));

        webTestClient.get().uri("/api/books")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].title").isEqualTo("BookTitle_1")
                .jsonPath("$[0].author.fullName").isEqualTo("Author_1")
                .jsonPath("$[0].genres[0].name").isEqualTo("Genre_1");
    }

    @DisplayName("должен возвращать книгу по id")
    @Test
    void shouldReturnBookById() {
        given(bookRepository.findById(1L)).willReturn(Mono.just(book));

        webTestClient.get().uri("/api/books/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.title").isEqualTo("BookTitle_1");
    }

    @DisplayName("должен возвращать 404 при отсутствии книги")
    @Test
    void shouldReturn404WhenBookNotFound() {
        given(bookRepository.findById(100L)).willReturn(Mono.empty());

        webTestClient.get().uri("/api/books/100")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.detail").isEqualTo("Book with id 100 not found");
    }

    @DisplayName("должен создавать книгу и возвращать 201")
    @Test
    void shouldCreateBook() {
        var createDto = new BookCreateDto("New Book", 1L, Set.of(1L));
        given(authorRepository.findById(1L)).willReturn(Mono.just(author));
        given(bookRepository.save(any(Book.class))).willReturn(Mono.just(book));
        given(bookGenreRelationRepository.saveAll(1L, Set.of(1L)))
                .willReturn(Flux.just(new BookGenreRelation(1L, 1L)));

        webTestClient.post().uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1);
    }

    @DisplayName("должен обновлять книгу")
    @Test
    void shouldUpdateBook() {
        var updateDto = new BookUpdateDto(0, "Updated Book", 1L, Set.of(1L));
        given(bookRepository.findById(1L)).willReturn(Mono.just(book));
        given(authorRepository.findById(1L)).willReturn(Mono.just(author));
        given(bookRepository.save(any(Book.class))).willReturn(Mono.just(book));
        given(bookGenreRelationRepository.deleteAllByBookId(1L)).willReturn(Mono.empty());
        given(bookGenreRelationRepository.saveAll(1L, Set.of(1L)))
                .willReturn(Flux.just(new BookGenreRelation(1L, 1L)));

        webTestClient.put().uri("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDto)
                .exchange()
                .expectStatus().isOk();
    }

    @DisplayName("должен удалять книгу и возвращать 204")
    @Test
    void shouldDeleteBook() {
        given(bookRepository.deleteById(1L)).willReturn(Mono.empty());

        webTestClient.delete().uri("/api/books/1")
                .exchange()
                .expectStatus().isNoContent();

        verify(bookRepository).deleteById(1L);
    }

    @DisplayName("не должен создавать книгу с пустым названием")
    @Test
    void shouldNotCreateBookWithBlankTitle() {
        var invalid = new BookCreateDto("", 1L, Set.of(1L));

        webTestClient.post().uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalid)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors.title").exists();
    }

    @DisplayName("не должен создавать книгу без жанров")
    @Test
    void shouldNotCreateBookWithoutGenres() {
        var invalid = new BookCreateDto("Title", 1L, Set.of());

        webTestClient.post().uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalid)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors.genreIds").exists();
    }
}
