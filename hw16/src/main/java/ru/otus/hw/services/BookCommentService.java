package ru.otus.hw.services;

import ru.otus.hw.services.dto.BookCommentCreateDto;
import ru.otus.hw.services.dto.BookCommentDto;
import ru.otus.hw.services.dto.BookCommentUpdateDto;

import java.util.List;

public interface BookCommentService {
    BookCommentDto findById(long id);

    List<BookCommentDto> findAllByBookId(long bookId);

    BookCommentDto insert(BookCommentCreateDto bookCommentCreateDto);

    BookCommentDto update(BookCommentUpdateDto bookCommentUpdateDto);

    void deleteById(long id);
}
