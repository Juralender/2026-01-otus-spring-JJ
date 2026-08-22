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
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.services.dto.AuthorCreateDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@WebFluxTest(AuthorRestController.class)
class AuthorRestControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AuthorRepository authorRepository;

    private Author author;

    @BeforeEach
    void setUp() {
        author = new Author(1, "Author_1");
    }

    @DisplayName("должен возвращать список авторов")
    @Test
    void shouldReturnAuthorsList() {
        given(authorRepository.findAll()).willReturn(Flux.just(author));

        webTestClient.get().uri("/api/authors")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].fullName").isEqualTo("Author_1");
    }

    @DisplayName("должен возвращать автора по id")
    @Test
    void shouldReturnAuthorById() {
        given(authorRepository.findById(1L)).willReturn(Mono.just(author));

        webTestClient.get().uri("/api/authors/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.fullName").isEqualTo("Author_1");
    }

    @DisplayName("должен возвращать 404 при отсутствии автора")
    @Test
    void shouldReturn404WhenAuthorNotFound() {
        given(authorRepository.findById(100L)).willReturn(Mono.empty());

        webTestClient.get().uri("/api/authors/100")
                .exchange()
                .expectStatus().isNotFound();
    }

    @DisplayName("должен создавать автора и возвращать 201")
    @Test
    void shouldCreateAuthor() {
        var createDto = new AuthorCreateDto("New Author");
        given(authorRepository.save(any(Author.class))).willReturn(Mono.just(author));

        webTestClient.post().uri("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createDto)
                .exchange()
                .expectStatus().isCreated();
    }

    @DisplayName("должен обновлять автора")
    @Test
    void shouldUpdateAuthor() {
        var createDto = new AuthorCreateDto("Updated Author");
        given(authorRepository.findById(1L)).willReturn(Mono.just(author));
        given(authorRepository.save(any(Author.class))).willReturn(Mono.just(author));

        webTestClient.put().uri("/api/authors/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createDto)
                .exchange()
                .expectStatus().isOk();
    }

    @DisplayName("не должен создавать автора с пустым именем")
    @Test
    void shouldNotCreateAuthorWithBlankFullName() {
        var invalid = new AuthorCreateDto("");

        webTestClient.post().uri("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalid)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors.fullName").exists();
    }
}
