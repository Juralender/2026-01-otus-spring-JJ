package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.converters.BookCommentConverter;
import ru.otus.hw.services.BookCommentService;

import java.util.stream.Collectors;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
@RequiredArgsConstructor
@ShellComponent
public class BookCommentCommands {

    private final BookCommentService service;

    private final BookCommentConverter converter;

    @ShellMethod(value = "Find comment by id", key = "cbid")
    public String findCommentById(long id) {
        return service.findById(id)
                .map(converter::bookCommentToString)
                .orElse("Comment with id %d not found".formatted(id));
    }

    @ShellMethod(value = "Find all comments for a book", key = "acb")
    public String findAllCommentsByBookId(long bookId) {
        return service.findAllByBookId(bookId).stream()
                .map(converter::bookCommentToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

    @ShellMethod(value = "Insert comment", key = "cins")
    public String insertComment(String text, long bookId) {
        var savedComment = service.insert(text, bookId);
        return converter.bookCommentToString(savedComment);
    }

    @ShellMethod(value = "Update comment", key = "cupd")
    public String updateComment(long id, String text) {
        var savedComment = service.update(id, text);
        return converter.bookCommentToString(savedComment);
    }

    @ShellMethod(value = "Delete comment by id", key = "cdel")
    public void deleteComment(long id) {
        service.deleteById(id);
    }
}
