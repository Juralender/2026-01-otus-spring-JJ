package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import reactor.test.StepVerifier;
import ru.otus.hw.models.Author;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе R2DBC для работы с авторами")
@DataR2dbcTest
class AuthorRepositoryTest {

    @Autowired
    private AuthorRepository repository;

    @DisplayName("должен загружать автора по id")
    @ParameterizedTest
    @MethodSource("getDbAuthors")
    void shouldReturnCorrectAuthorById(Author expectedAuthor) {
        StepVerifier.create(repository.findById(expectedAuthor.getId()))
                .assertNext(actual -> assertThat(actual).usingRecursiveComparison().isEqualTo(expectedAuthor))
                .verifyComplete();
    }

    @DisplayName("должен возвращать пустой Mono, если автор не найден")
    @Test
    void shouldReturnEmptyMonoForUnknownId() {
        StepVerifier.create(repository.findById(10_000L))
                .verifyComplete();
    }

    @DisplayName("должен загружать список всех авторов")
    @Test
    void shouldReturnCorrectAuthorsList() {
        StepVerifier.create(repository.findAll().collectList())
                .assertNext(actual -> assertThat(actual)
                        .usingRecursiveFieldByFieldElementComparator()
                        .containsExactlyElementsOf(getDbAuthors()))
                .verifyComplete();
    }

    @DisplayName("должен загружать авторов по набору id")
    @Test
    void shouldReturnCorrectAuthorsByIds() {
        StepVerifier.create(repository.findAllByIdIn(Set.of(1L, 3L)).collectList())
                .assertNext(actual -> assertThat(actual)
                        .usingRecursiveFieldByFieldElementComparator()
                        .containsExactlyInAnyOrder(getDbAuthors().get(0), getDbAuthors().get(2)))
                .verifyComplete();
    }

    private static List<Author> getDbAuthors() {
        return IntStream.range(1, 4).boxed()
                .map(id -> new Author(id, "Author_" + id))
                .toList();
    }
}
