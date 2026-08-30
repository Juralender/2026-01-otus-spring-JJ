package ru.otus.hw.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import ru.otus.hw.models.Book;

public interface BookRepository extends R2dbcRepository<Book, Long> {

    @Override
    default Flux<Book> findAll() {
        return findAll(Sort.by("id"));
    }
}
