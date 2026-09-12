package ru.otus.hw.rest.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.services.BookCommentService;
import ru.otus.hw.services.dto.BookCommentCreateDto;
import ru.otus.hw.services.dto.BookCommentDto;

import java.util.List;

@RestController
@RequestMapping("/api/books/{bookId}/comments")
@RequiredArgsConstructor
public class BookCommentRestController {

    private final BookCommentService bookCommentService;

    @GetMapping
    public List<BookCommentDto> findAllByBookId(@PathVariable long bookId) {
        return bookCommentService.findAllByBookId(bookId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookCommentDto create(@PathVariable long bookId, @Valid @RequestBody BookCommentCreateDto request) {
        return bookCommentService.insert(new BookCommentCreateDto(request.getText(), bookId));
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long bookId, @PathVariable long commentId) {
        bookCommentService.deleteById(commentId);
    }
}
