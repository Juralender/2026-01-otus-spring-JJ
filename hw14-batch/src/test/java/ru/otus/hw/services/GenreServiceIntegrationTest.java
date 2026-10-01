package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.GenreRepository;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Интеграционные тесты сервиса жанров")
@DataMongoTest
@Import(GenreServiceImpl.class)
class GenreServiceIntegrationTest {

    @Autowired
    private GenreService genreService;

    @Autowired
    private GenreRepository genreRepository;

    @BeforeEach
    void setUp() {
        genreRepository.deleteAll();
        genreRepository.saveAll(List.of(
                new Genre(null, "Genre_1"),
                new Genre(null, "Genre_2"),
                new Genre(null, "Genre_3")));
    }

    @DisplayName("должен загружать список всех жанров, отсортированный по id")
    @Test
    void shouldReturnAllGenresSortedById() {
        var expectedGenres = genreRepository.findAll().stream()
                .sorted(Comparator.comparing(Genre::getId))
                .toList();

        var actualGenres = genreService.findAll();

        assertThat(actualGenres).containsExactlyElementsOf(expectedGenres);
    }
}
