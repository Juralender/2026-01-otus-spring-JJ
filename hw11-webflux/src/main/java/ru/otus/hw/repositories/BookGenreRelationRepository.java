package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.BookGenreRelation;

import java.util.Collection;
import java.util.Set;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class BookGenreRelationRepository {

    private final R2dbcEntityOperations entityOperations;

    public Flux<BookGenreRelation> findAllByBookIdIn(Collection<Long> bookIds) {
        return entityOperations.select(
                query(where("book_id").in(bookIds)),
                BookGenreRelation.class);
    }

    public Mono<Void> deleteAllByBookId(long bookId) {
        return entityOperations.delete(
                query(where("book_id").is(bookId)),
                BookGenreRelation.class).then();
    }

    public Flux<BookGenreRelation> saveAll(long bookId, Set<Long> genreIds) {
        return Flux.fromIterable(genreIds)
                .map(genreId -> new BookGenreRelation(bookId, genreId))
                .concatMap(entityOperations::insert);
    }
}
