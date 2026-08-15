package ru.otus.hw.services;

import ru.otus.hw.models.BookComment;

import java.util.List;
import java.util.Optional;

public interface BookCommentService {
    Optional<BookComment> findById(String id);

    List<BookComment> findAllByBookId(String bookId);

    BookComment insert(String text, String bookId);

    BookComment update(String id, String text);

    void deleteById(String id);
}
