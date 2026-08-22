package ru.otus.hw.rest.handlers;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.GenreRepository;
import ru.otus.hw.services.dto.GenreCreateDto;
import ru.otus.hw.services.dto.GenreDto;

@Component
@RequiredArgsConstructor
public class GenreHandler {

    private final GenreRepository genreRepository;

    private final Validator validator;

    @Transactional(readOnly = true)
    public Mono<ServerResponse> findAll(ServerRequest request) {
        return ServerResponse.ok().body(genreRepository.findAll().map(this::toDto), GenreDto.class);
    }

    @Transactional(readOnly = true)
    public Mono<ServerResponse> findById(ServerRequest request) {
        var id = Long.parseLong(request.pathVariable("id"));
        return genreRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Genre with id %d not found".formatted(id))))
                .map(this::toDto)
                .flatMap(dto -> ServerResponse.ok().bodyValue(dto));
    }

    @Transactional
    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(GenreCreateDto.class)
                .doOnNext(this::validate)
                .flatMap(dto -> genreRepository.save(new Genre(0, dto.getName())))
                .map(this::toDto)
                .flatMap(dto -> ServerResponse.status(HttpStatus.CREATED).bodyValue(dto));
    }

    @Transactional
    public Mono<ServerResponse> update(ServerRequest request) {
        var id = Long.parseLong(request.pathVariable("id"));
        return request.bodyToMono(GenreCreateDto.class)
                .doOnNext(this::validate)
                .flatMap(dto -> genreRepository.findById(id)
                        .switchIfEmpty(Mono.error(
                                new EntityNotFoundException("Genre with id %d not found".formatted(id))))
                        .flatMap(genre -> {
                            genre.setName(dto.getName());
                            return genreRepository.save(genre);
                        }))
                .map(this::toDto)
                .flatMap(dto -> ServerResponse.ok().bodyValue(dto));
    }

    private void validate(GenreCreateDto dto) {
        var violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private GenreDto toDto(Genre genre) {
        return new GenreDto(genre.getId(), genre.getName());
    }
}
