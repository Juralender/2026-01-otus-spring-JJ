package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import reactor.test.StepVerifier;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе R2DBC для работы с книгами")
@DataR2dbcTest
class BookRepositoryTest {

    @Autowired
    private BookRepository repository;

    @DisplayName("должен загружать книгу по id")
    @ParameterizedTest
    @MethodSource("getDbBooks")
    void shouldReturnCorrectBookById(Book expectedBook) {
        StepVerifier.create(repository.findById(expectedBook.getId()))
                .assertNext(actual -> assertThat(actual).usingRecursiveComparison().isEqualTo(expectedBook))
                .verifyComplete();
    }

    @DisplayName("должен возвращать пустой Mono, если книга не найдена")
    @Test
    void shouldReturnEmptyMonoForUnknownId() {
        StepVerifier.create(repository.findById(10_000L))
                .verifyComplete();
    }

    @DisplayName("должен загружать список всех книг")
    @Test
    void shouldReturnAllBooks() {
        StepVerifier.create(repository.findAll().collectList())
                .assertNext(actual -> assertThat(actual)
                        .usingRecursiveFieldByFieldElementComparator()
                        .containsAll(getDbBooks()))
                .verifyComplete();
    }

    @DisplayName("должен сохранять новую книгу")
    @Test
    void shouldSaveNewBook() {
        var newBook = new Book(0, "BookTitle_10500", 1L);

        StepVerifier.create(repository.save(newBook)
                        .flatMap(saved -> repository.findById(saved.getId())))
                .assertNext(actual -> {
                    assertThat(actual.getId()).isGreaterThan(0);
                    assertThat(actual.getTitle()).isEqualTo("BookTitle_10500");
                    assertThat(actual.getAuthorId()).isEqualTo(1L);
                })
                .verifyComplete();
    }

    @DisplayName("должен сохранять измененную книгу")
    @Test
    void shouldSaveUpdatedBook() {
        StepVerifier.create(repository.save(new Book(0, "Draft title", 1L))
                        .flatMap(saved -> {
                            saved.setTitle("BookTitle_10500");
                            saved.setAuthorId(3L);
                            return repository.save(saved);
                        }))
                .assertNext(actual -> {
                    assertThat(actual.getTitle()).isEqualTo("BookTitle_10500");
                    assertThat(actual.getAuthorId()).isEqualTo(3L);
                })
                .verifyComplete();
    }

    @DisplayName("должен удалять книгу по id")
    @Test
    void shouldDeleteBook() {
        StepVerifier.create(repository.save(new Book(0, "To delete", 1L))
                        .flatMap(saved -> repository.deleteById(saved.getId()).thenReturn(saved))
                        .flatMap(saved -> repository.findById(saved.getId())))
                .verifyComplete();
    }

    private static List<Book> getDbBooks() {
        return IntStream.range(1, 4).boxed()
                .map(id -> new Book(id, "BookTitle_" + id, id))
                .toList();
    }
}
