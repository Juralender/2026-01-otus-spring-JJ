package ru.otus.hw.services;

import ru.otus.hw.services.dto.AuthorCreateDto;
import ru.otus.hw.services.dto.AuthorDto;
import ru.otus.hw.services.dto.AuthorUpdateDto;

import java.util.List;

public interface AuthorService {
    List<AuthorDto> findAll();

    AuthorDto findById(long id);

    AuthorDto insert(AuthorCreateDto authorCreateDto);

    AuthorDto update(AuthorUpdateDto authorUpdateDto);
}
