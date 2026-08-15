package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Author;
import ru.otus.hw.repositories.AuthorRepository;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Интеграционные тесты сервиса авторов")
@DataMongoTest
@Import(AuthorServiceImpl.class)
class AuthorServiceIntegrationTest {

    @Autowired
    private AuthorService authorService;

    @Autowired
    private AuthorRepository authorRepository;

    @BeforeEach
    void setUp() {
        authorRepository.deleteAll();
        authorRepository.saveAll(List.of(
                new Author(null, "Author_1"),
                new Author(null, "Author_2"),
                new Author(null, "Author_3")));
    }

    @DisplayName("должен загружать список всех авторов, отсортированный по id")
    @Test
    void shouldReturnAllAuthorsSortedById() {
        var expectedAuthors = authorRepository.findAll().stream()
                .sorted(Comparator.comparing(Author::getId))
                .toList();

        var actualAuthors = authorService.findAll();

        assertThat(actualAuthors).containsExactlyElementsOf(expectedAuthors);
    }
}
