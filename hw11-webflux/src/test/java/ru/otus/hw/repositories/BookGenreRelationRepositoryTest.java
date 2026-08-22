package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;
import ru.otus.hw.models.BookGenreRelation;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Кастомный репозиторий связки книга-жанр")
@DataR2dbcTest
@Import(BookGenreRelationRepository.class)
class BookGenreRelationRepositoryTest {

    @Autowired
    private BookGenreRelationRepository repository;

    @DisplayName("должен находить связи по набору id книг одним батчем")
    @Test
    void shouldFindAllByBookIdIn() {
        StepVerifier.create(repository.findAllByBookIdIn(List.of(1L, 2L)).collectList())
                .assertNext(actual -> assertThat(actual)
                        .extracting(BookGenreRelation::getBookId, BookGenreRelation::getGenreId)
                        .containsExactlyInAnyOrder(
                                org.assertj.core.groups.Tuple.tuple(1L, 1L),
                                org.assertj.core.groups.Tuple.tuple(1L, 2L),
                                org.assertj.core.groups.Tuple.tuple(2L, 3L),
                                org.assertj.core.groups.Tuple.tuple(2L, 4L)))
                .verifyComplete();
    }

    @DisplayName("должен сохранять и удалять связи книги с жанрами")
    @Test
    void shouldSaveAndDeleteRelationsForBook() {
        var setup = repository.saveAll(3L, Set.of(1L, 2L)).then();

        StepVerifier.create(setup.thenMany(repository.findAllByBookIdIn(List.of(3L))))
                .expectNextCount(4)
                .verifyComplete();

        StepVerifier.create(repository.deleteAllByBookId(3L)
                        .thenMany(repository.findAllByBookIdIn(List.of(3L))))
                .verifyComplete();
    }
}
