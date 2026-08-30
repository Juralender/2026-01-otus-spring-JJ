package ru.otus.hw.rest.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.services.dto.AuthorCreateDto;
import ru.otus.hw.services.dto.AuthorDto;
import ru.otus.hw.services.dto.AuthorUpdateDto;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorRestController {

    private final AuthorRepository authorRepository;

    @Transactional(readOnly = true)
    @GetMapping
    public Flux<AuthorDto> findAll() {
        return authorRepository.findAll().map(this::toDto);
    }

    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    public Mono<AuthorDto> findById(@PathVariable long id) {
        return authorRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Author with id %d not found".formatted(id))))
                .map(this::toDto);
    }

    @Transactional
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AuthorDto> create(@Valid @RequestBody AuthorCreateDto authorCreateDto) {
        return authorRepository.save(new Author(0, authorCreateDto.getFullName()))
                .map(this::toDto);
    }

    @Transactional
    @PutMapping("/{id}")
    public Mono<AuthorDto> update(@PathVariable long id, @Valid @RequestBody AuthorCreateDto authorCreateDto) {
        var authorUpdateDto = new AuthorUpdateDto(id, authorCreateDto.getFullName());
        return authorRepository.findById(authorUpdateDto.getId())
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Author with id %d not found".formatted(authorUpdateDto.getId()))))
                .flatMap(author -> {
                    author.setFullName(authorUpdateDto.getFullName());
                    return authorRepository.save(author);
                })
                .map(this::toDto);
    }

    private AuthorDto toDto(Author author) {
        return new AuthorDto(author.getId(), author.getFullName());
    }
}
