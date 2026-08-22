package ru.otus.hw.rest.routers;

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
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.GenreRepository;
import ru.otus.hw.rest.handlers.GenreHandler;
import ru.otus.hw.services.dto.GenreCreateDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@WebFluxTest({GenreRouter.class, GenreHandler.class})
class GenreRouterTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private GenreRepository genreRepository;

    private Genre genre;

    @BeforeEach
    void setUp() {
        genre = new Genre(1, "Genre_1");
    }

    @DisplayName("должен возвращать список жанров")
    @Test
    void shouldReturnGenresList() {
        given(genreRepository.findAll()).willReturn(Flux.just(genre));

        webTestClient.get().uri("/api/genres")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].name").isEqualTo("Genre_1");
    }

    @DisplayName("должен возвращать жанр по id")
    @Test
    void shouldReturnGenreById() {
        given(genreRepository.findById(1L)).willReturn(Mono.just(genre));

        webTestClient.get().uri("/api/genres/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Genre_1");
    }

    @DisplayName("должен возвращать 404 при отсутствии жанра")
    @Test
    void shouldReturn404WhenGenreNotFound() {
        given(genreRepository.findById(100L)).willReturn(Mono.empty());

        webTestClient.get().uri("/api/genres/100")
                .exchange()
                .expectStatus().isNotFound();
    }

    @DisplayName("должен создавать жанр и возвращать 201")
    @Test
    void shouldCreateGenre() {
        var createDto = new GenreCreateDto("New Genre");
        given(genreRepository.save(any(Genre.class))).willReturn(Mono.just(genre));

        webTestClient.post().uri("/api/genres")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createDto)
                .exchange()
                .expectStatus().isCreated();
    }

    @DisplayName("должен обновлять жанр")
    @Test
    void shouldUpdateGenre() {
        var createDto = new GenreCreateDto("Updated");
        given(genreRepository.findById(1L)).willReturn(Mono.just(genre));
        given(genreRepository.save(any(Genre.class))).willReturn(Mono.just(genre));

        webTestClient.put().uri("/api/genres/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createDto)
                .exchange()
                .expectStatus().isOk();
    }

    @DisplayName("не должен создавать жанр с названием длиннее 10 символов")
    @Test
    void shouldNotCreateGenreWithTooLongName() {
        var invalid = new GenreCreateDto("a".repeat(11));

        webTestClient.post().uri("/api/genres")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalid)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors.name").exists();
    }
}
