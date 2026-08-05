package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Jpa для работы с жанрами")
@DataJpaTest
class GenreRepositoryTest {

    @Autowired
    private GenreRepository repository;

    @Autowired
    private TestEntityManager testEntityManager;

    @DisplayName("должен загружать список всех жанров")
    @Test
    void shouldReturnCorrectGenresList() {
        var actualGenres = repository.findAll();
        assertThat(actualGenres).containsExactlyElementsOf(getDbGenres());
    }

    @DisplayName("должен загружать жанры по набору id")
    @Test
    void shouldReturnCorrectGenresByIds() {
        var expectedGenres = List.of(
                testEntityManager.find(Genre.class, 1L),
                testEntityManager.find(Genre.class, 3L),
                testEntityManager.find(Genre.class, 5L));
        var ids = expectedGenres.stream().map(Genre::getId).collect(Collectors.toSet());

        var actualGenres = repository.findAllByIdIn(ids);

        assertThat(actualGenres).containsExactlyInAnyOrderElementsOf(expectedGenres);
    }

    @DisplayName("должен возвращать пустой список, если жанры с переданными id не найдены")
    @Test
    void shouldReturnEmptyListForUnknownIds() {
        var actualGenres = repository.findAllByIdIn(Set.of(10_000L));
        assertThat(actualGenres).isEmpty();
    }

    private static List<Genre> getDbGenres() {
        return IntStream.range(1, 7).boxed()
                .map(id -> new Genre(id, "Genre_" + id))
                .toList();
    }
}
