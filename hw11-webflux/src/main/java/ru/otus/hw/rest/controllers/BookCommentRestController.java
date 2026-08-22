package ru.otus.hw.rest.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.repositories.BookCommentRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.services.dto.BookCommentCreateDto;
import ru.otus.hw.services.dto.BookCommentDto;

@RestController
@RequestMapping("/api/books/{bookId}/comments")
@RequiredArgsConstructor
public class BookCommentRestController {

    private final BookRepository bookRepository;

    private final BookCommentRepository bookCommentRepository;

    @Transactional(readOnly = true)
    @GetMapping
    public Flux<BookCommentDto> findAllByBookId(@PathVariable long bookId) {
        return bookCommentRepository.findAllByBookIdOrderByIdAsc(bookId).map(this::toDto);
    }

    @Transactional
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<BookCommentDto> create(@PathVariable long bookId, @Valid @RequestBody BookCommentCreateDto request) {
        return bookRepository.findById(bookId)
                .switchIfEmpty(Mono.error(
                        new EntityNotFoundException("Book with id %d not found".formatted(bookId))))
                .flatMap(book -> bookCommentRepository.save(new BookComment(request.getText(), book.getId())))
                .map(this::toDto);
    }

    @Transactional
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable long bookId, @PathVariable long commentId) {
        return bookCommentRepository.deleteById(commentId);
    }

    private BookCommentDto toDto(BookComment comment) {
        return new BookCommentDto(comment.getId(), comment.getText(), comment.getBookId());
    }
}
