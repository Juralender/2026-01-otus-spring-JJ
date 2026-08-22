package ru.otus.hw.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import ru.otus.hw.models.Genre;

import java.util.Set;

public interface GenreRepository extends R2dbcRepository<Genre, Long> {

    @Override
    default Flux<Genre> findAll() {
        return findAll(Sort.by("id"));
    }

    Flux<Genre> findAllByIdIn(Set<Long> ids);
}
