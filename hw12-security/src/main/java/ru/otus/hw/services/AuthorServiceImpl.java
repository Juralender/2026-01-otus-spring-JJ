package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.services.dto.AuthorCreateDto;
import ru.otus.hw.services.dto.AuthorDto;
import ru.otus.hw.services.dto.AuthorUpdateDto;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;

    @Transactional(readOnly = true)
    @Override
    public List<AuthorDto> findAll() {
        return authorRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public AuthorDto findById(long id) {
        return authorRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Author with id %d not found".formatted(id)));
    }

    @Transactional
    @Override
    public AuthorDto insert(AuthorCreateDto authorCreateDto) {
        var author = authorRepository.save(new Author(0, authorCreateDto.getFullName()));
        return toDto(author);
    }

    @Transactional
    @Override
    public AuthorDto update(AuthorUpdateDto authorUpdateDto) {
        var author = authorRepository.findById(authorUpdateDto.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Author with id %d not found".formatted(authorUpdateDto.getId())));
        author.setFullName(authorUpdateDto.getFullName());
        return toDto(authorRepository.save(author));
    }

    private AuthorDto toDto(Author author) {
        return new AuthorDto(author.getId(), author.getFullName());
    }
}
