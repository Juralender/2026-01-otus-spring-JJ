package ru.otus.hw.rest.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.repositories.BookCommentRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.services.dto.BookCommentCreateDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@WebFluxTest(BookCommentRestController.class)
class BookCommentRestControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private BookRepository bookRepository;

    @MockitoBean
    private BookCommentRepository bookCommentRepository;

    @DisplayName("должен возвращать список комментариев книги")
    @Test
    void shouldReturnCommentsList() {
        given(bookCommentRepository.findAllByBookIdOrderByIdAsc(1L))
                .willReturn(Flux.just(new BookComment(1, "Comment", 1L)));

        webTestClient.get().uri("/api/books/1/comments")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].text").isEqualTo("Comment");
    }

    @DisplayName("должен добавлять комментарий и возвращать 201")
    @Test
    void shouldCreateComment() {
        var requestBody = new BookCommentCreateDto("New comment", 0L);
        given(bookRepository.findById(1L)).willReturn(Mono.just(new Book(1, "BookTitle_1", 1L)));
        given(bookCommentRepository.save(any(BookComment.class)))
                .willReturn(Mono.just(new BookComment(5, "New comment", 1L)));

        webTestClient.post().uri("/api/books/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.text").isEqualTo("New comment");
    }

    @DisplayName("не должен добавлять пустой комментарий")
    @Test
    void shouldNotCreateBlankComment() {
        var requestBody = new BookCommentCreateDto("", 0L);

        webTestClient.post().uri("/api/books/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors.text").exists();
    }

    @DisplayName("должен удалять комментарий и возвращать 204")
    @Test
    void shouldDeleteComment() {
        given(bookCommentRepository.deleteById(5L)).willReturn(Mono.empty());

        webTestClient.delete().uri("/api/books/1/comments/5")
                .exchange()
                .expectStatus().isNoContent();

        verify(bookCommentRepository).deleteById(5L);
    }
}
