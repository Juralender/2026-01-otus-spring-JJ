package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.otus.hw.models.Author;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Jpa для работы с авторами")
@DataJpaTest
class AuthorRepositoryTest {

    @Autowired
    private AuthorRepository repository;

    @Autowired
    private TestEntityManager testEntityManager;

    @DisplayName("должен загружать автора по id")
    @ParameterizedTest
    @MethodSource("getDbAuthors")
    void shouldReturnCorrectAuthorById(Author expectedAuthor) {
        var expectedFromDb = testEntityManager.find(Author.class, expectedAuthor.getId());

        var actualAuthor = repository.findById(expectedAuthor.getId());

        assertThat(actualAuthor).isPresent().get().isEqualTo(expectedFromDb);
    }

    @DisplayName("должен возвращать пустой Optional, если автор не найден")
    @Test
    void shouldReturnEmptyOptionalForUnknownId() {
        assertThat(repository.findById(10_000L)).isEmpty();
    }

    @DisplayName("должен загружать список всех авторов")
    @Test
    void shouldReturnCorrectAuthorsList() {
        var actualAuthors = repository.findAll();
        assertThat(actualAuthors).containsExactlyElementsOf(getDbAuthors());
    }

    private static List<Author> getDbAuthors() {
        return IntStream.range(1, 4).boxed()
                .map(id -> new Author(id, "Author_" + id))
                .toList();
    }
}
