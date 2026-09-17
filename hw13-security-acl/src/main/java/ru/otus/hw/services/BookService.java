package ru.otus.hw.services;

import ru.otus.hw.services.dto.BookCreateDto;
import ru.otus.hw.services.dto.BookDto;
import ru.otus.hw.services.dto.BookUpdateDto;

import java.util.List;

public interface BookService {
    BookDto findById(long id);

    List<BookDto> findAll();

    BookDto insert(BookCreateDto bookCreateDto);

    BookDto update(BookUpdateDto bookUpdateDto);

    void deleteById(long id);
}
