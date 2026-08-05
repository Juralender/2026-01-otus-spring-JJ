package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Jpa для работы с книгами")
@DataJpaTest
@Import(JpaBookRepository.class)
class BookRepositoryTest {

    @Autowired
    private JpaBookRepository repository;

    @Autowired
    private TestEntityManager testEntityManager;

    @DisplayName("должен загружать книгу по id со всеми связями")
    @ParameterizedTest
    @MethodSource("getDbBooks")
    void shouldReturnCorrectBookById(Book expectedBook) {
        var actualBook = repository.findById(expectedBook.getId());

        assertThat(actualBook).isPresent();
        assertThat(actualBook.get()).usingRecursiveComparison().isEqualTo(expectedBook);
    }

    @DisplayName("должен возвращать пустой Optional, если книга не найдена")
    @Test
    void shouldReturnEmptyOptionalForUnknownId() {
        assertThat(repository.findById(10_000L)).isEmpty();
    }

    @DisplayName("должен загружать список всех книг со всеми связями")
    @Test
    void shouldReturnCorrectBooksList() {
        var actualBooks = repository.findAll();
        assertThat(actualBooks).usingRecursiveComparison().isEqualTo(getDbBooks());
    }

    @DisplayName("должен сохранять новую книгу")
    @Test
    void shouldSaveNewBook() {
        var author = testEntityManager.find(Author.class, 1L);
        var genres = List.of(testEntityManager.find(Genre.class, 1L), testEntityManager.find(Genre.class, 3L));
        var newBook = new Book(0, "BookTitle_10500", author, genres);

        var savedBook = repository.save(newBook);
        testEntityManager.flush();
        testEntityManager.clear();

        assertThat(savedBook.getId()).isGreaterThan(0);

        var bookFromDb = testEntityManager.find(Book.class, savedBook.getId());
        assertThat(bookFromDb).isNotNull();
        assertThat(bookFromDb.getTitle()).isEqualTo("BookTitle_10500");
        assertThat(bookFromDb.getAuthor().getId()).isEqualTo(author.getId());
        assertThat(bookFromDb.getGenres()).extracting(Genre::getId)
                .containsExactlyInAnyOrder(1L, 3L);
    }

    @DisplayName("должен сохранять измененную книгу")
    @Test
    void shouldSaveUpdatedBook() {
        var newAuthor = testEntityManager.find(Author.class, 3L);
        var newGenres = List.of(testEntityManager.find(Genre.class, 5L), testEntityManager.find(Genre.class, 6L));
        var updatedBook = new Book(1L, "BookTitle_10500", newAuthor, newGenres);

        repository.save(updatedBook);
        testEntityManager.flush();
        testEntityManager.clear();

        var bookFromDb = testEntityManager.find(Book.class, 1L);
        assertThat(bookFromDb.getTitle()).isEqualTo("BookTitle_10500");
        assertThat(bookFromDb.getAuthor().getId()).isEqualTo(3L);
        assertThat(bookFromDb.getGenres()).extracting(Genre::getId)
                .containsExactlyInAnyOrder(5L, 6L);
    }

    @DisplayName("должен удалять книгу по id")
    @Test
    void shouldDeleteBook() {
        assertThat(testEntityManager.find(Book.class, 1L)).isNotNull();

        repository.deleteById(1L);
        testEntityManager.flush();
        testEntityManager.clear();

        assertThat(testEntityManager.find(Book.class, 1L)).isNull();
    }

    private static List<Author> getDbAuthors() {
        return IntStream.range(1, 4).boxed()
                .map(id -> new Author(id, "Author_" + id))
                .toList();
    }

    private static List<Genre> getDbGenres() {
        return IntStream.range(1, 7).boxed()
                .map(id -> new Genre(id, "Genre_" + id))
                .toList();
    }

    private static List<Book> getDbBooks() {
        var dbAuthors = getDbAuthors();
        var dbGenres = getDbGenres();
        return IntStream.range(1, 4).boxed()
                .map(id -> new Book(id,
                        "BookTitle_" + id,
                        dbAuthors.get(id - 1),
                        dbGenres.subList((id - 1) * 2, (id - 1) * 2 + 2)
                ))
                .toList();
    }
}
