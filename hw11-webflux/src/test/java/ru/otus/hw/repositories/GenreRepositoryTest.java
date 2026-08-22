package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import reactor.test.StepVerifier;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе R2DBC для работы с жанрами")
@DataR2dbcTest
class GenreRepositoryTest {

    @Autowired
    private GenreRepository repository;

    @DisplayName("должен загружать список всех жанров")
    @Test
    void shouldReturnCorrectGenresList() {
        StepVerifier.create(repository.findAll().collectList())
                .assertNext(actual -> assertThat(actual).containsExactlyElementsOf(getDbGenres()))
                .verifyComplete();
    }

    @DisplayName("должен загружать жанры по набору id")
    @Test
    void shouldReturnCorrectGenresByIds() {
        var dbGenres = getDbGenres();
        var expectedGenres = List.of(dbGenres.get(0), dbGenres.get(2), dbGenres.get(4));
        var ids = Set.of(1L, 3L, 5L);

        StepVerifier.create(repository.findAllByIdIn(ids).collectList())
                .assertNext(actual -> assertThat(actual).containsExactlyInAnyOrderElementsOf(expectedGenres))
                .verifyComplete();
    }

    @DisplayName("должен возвращать пустой список, если жанры с переданными id не найдены")
    @Test
    void shouldReturnEmptyListForUnknownIds() {
        StepVerifier.create(repository.findAllByIdIn(Set.of(10_000L)).collectList())
                .assertNext(actual -> assertThat(actual).isEmpty())
                .verifyComplete();
    }

    private static List<Genre> getDbGenres() {
        return IntStream.range(1, 7).boxed()
                .map(id -> new Genre(id, "Genre_" + id))
                .toList();
    }
}
