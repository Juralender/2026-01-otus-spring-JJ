package ru.otus.hw.services;

import ru.otus.hw.services.dto.GenreCreateDto;
import ru.otus.hw.services.dto.GenreDto;
import ru.otus.hw.services.dto.GenreUpdateDto;

import java.util.List;

public interface GenreService {
    List<GenreDto> findAll();

    GenreDto findById(long id);

    GenreDto insert(GenreCreateDto genreCreateDto);

    GenreDto update(GenreUpdateDto genreUpdateDto);
}
