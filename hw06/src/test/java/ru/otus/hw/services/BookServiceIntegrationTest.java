package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.JpaAuthorRepository;
import ru.otus.hw.repositories.JpaBookRepository;
import ru.otus.hw.repositories.JpaGenreRepository;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Интеграционные тесты сервиса книг")
@DataJpaTest
@Import({BookServiceImpl.class, JpaBookRepository.class, JpaAuthorRepository.class, JpaGenreRepository.class})
@Transactional(propagation = Propagation.NEVER)
class BookServiceIntegrationTest {

    @Autowired
    private BookService bookService;

    @DisplayName("должен позволять обращаться к автору и жанрам книги вне транзакции сервиса (findById)")
    @Test
    void shouldAllowAccessingRelationsAfterFindByIdOutsideTransaction() {
        var book = bookService.findById(1L).orElseThrow();

        assertThatCode(() -> {
            assertThat(book.getAuthor()).usingRecursiveComparison().isEqualTo(new Author(1L, "Author_1"));
            assertThat(book.getGenres()).extracting(Genre::getName)
                    .containsExactly("Genre_1", "Genre_2");
        }).doesNotThrowAnyException();
    }

    @DisplayName("должен позволять обращаться к автору и жанрам каждой книги вне транзакции сервиса (findAll)")
    @Test
    void shouldAllowAccessingRelationsAfterFindAllOutsideTransaction() {
        var books = bookService.findAll();

        assertThat(books).isNotEmpty();
        assertThatCode(() -> books.forEach(book -> {
            book.getAuthor().getFullName();
            book.getGenres().forEach(Genre::getName);
        })).doesNotThrowAnyException();
    }

    @DisplayName("должен позволять обращаться к связям только что добавленной книги вне транзакции сервиса")
    @Test
    void shouldAllowAccessingRelationsAfterInsertOutsideTransaction() {
        var savedBook = bookService.insert("Integration_Test_Book", 1L, Set.of(1L, 2L));
        try {
            assertThatCode(() -> {
                assertThat(savedBook.getAuthor()).usingRecursiveComparison().isEqualTo(new Author(1L, "Author_1"));
                assertThat(savedBook.getGenres()).extracting(Genre::getName)
                        .containsExactlyInAnyOrder("Genre_1", "Genre_2");
            }).doesNotThrowAnyException();
        } finally {
            bookService.deleteById(savedBook.getId());
        }
    }

    @DisplayName("должен позволять обращаться к связям обновленной книги вне транзакции сервиса")
    @Test
    void shouldAllowAccessingRelationsAfterUpdateOutsideTransaction() {
        var savedBook = bookService.insert("Integration_Test_Book_2", 1L, Set.of(1L));
        try {
            var updatedBook = bookService.update(savedBook.getId(), "Updated_Title", 2L, Set.of(3L, 4L));

            assertThatCode(() -> {
                assertThat(updatedBook.getAuthor()).usingRecursiveComparison().isEqualTo(new Author(2L, "Author_2"));
                assertThat(updatedBook.getGenres()).extracting(Genre::getName)
                        .containsExactlyInAnyOrder("Genre_3", "Genre_4");
            }).doesNotThrowAnyException();
        } finally {
            bookService.deleteById(savedBook.getId());
        }
    }

    @DisplayName("должен выбрасывать исключение при обновлении несуществующей книги")
    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentBook() {
        assertThatThrownBy(() -> bookService.update(10_000L, "Title", 1L, Set.of(1L)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @DisplayName("должен удалять книгу")
    @Test
    void shouldDeleteBook() {
        var savedBook = bookService.insert("Integration_Test_Book_3", 1L, Set.of(1L));

        bookService.deleteById(savedBook.getId());

        assertThat(bookService.findById(savedBook.getId())).isEmpty();
    }
}
