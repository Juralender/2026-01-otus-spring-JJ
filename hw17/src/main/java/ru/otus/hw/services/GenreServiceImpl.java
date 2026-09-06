package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.GenreRepository;
import ru.otus.hw.services.dto.GenreCreateDto;
import ru.otus.hw.services.dto.GenreDto;
import ru.otus.hw.services.dto.GenreUpdateDto;

import java.util.List;

@RequiredArgsConstructor
@Service
public class GenreServiceImpl implements GenreService {
    private final GenreRepository genreRepository;

    @Transactional(readOnly = true)
    @Override
    public List<GenreDto> findAll() {
        return genreRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public GenreDto findById(long id) {
        return genreRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Genre with id %d not found".formatted(id)));
    }

    @Transactional
    @Override
    public GenreDto insert(GenreCreateDto genreCreateDto) {
        var genre = genreRepository.save(new Genre(0, genreCreateDto.getName()));
        return toDto(genre);
    }

    @Transactional
    @Override
    public GenreDto update(GenreUpdateDto genreUpdateDto) {
        var genre = genreRepository.findById(genreUpdateDto.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Genre with id %d not found".formatted(genreUpdateDto.getId())));
        genre.setName(genreUpdateDto.getName());
        return toDto(genreRepository.save(genre));
    }

    private GenreDto toDto(Genre genre) {
        return new GenreDto(genre.getId(), genre.getName());
    }
}
