package ru.otus.hw.repositories;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import ru.otus.hw.models.BookComment;

public interface BookCommentRepository extends R2dbcRepository<BookComment, Long> {

    Flux<BookComment> findAllByBookIdOrderByIdAsc(long bookId);
}
