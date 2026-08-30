package ru.otus.hw.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import ru.otus.hw.models.Author;

import java.util.Collection;

public interface AuthorRepository extends R2dbcRepository<Author, Long> {

    @Override
    default Flux<Author> findAll() {
        return findAll(Sort.by("id"));
    }

    Flux<Author> findAllByIdIn(Collection<Long> ids);
}
