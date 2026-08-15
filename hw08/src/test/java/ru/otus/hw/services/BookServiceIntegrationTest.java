package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.listeners.BookCommentCascadeDeleteListener;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookCommentRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Интеграционные тесты сервиса книг")
@DataMongoTest
@Import({BookServiceImpl.class, BookCommentCascadeDeleteListener.class})
class BookServiceIntegrationTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookCommentRepository bookCommentRepository;

    private List<Author> authors;

    private List<Genre> genres;

    private List<Book> books;

    @BeforeEach
    void setUp() {
        bookCommentRepository.deleteAll();
        bookRepository.deleteAll();
        genreRepository.deleteAll();
        authorRepository.deleteAll();

        authors = authorRepository.saveAll(List.of(
                new Author(null, "Author_1"),
                new Author(null, "Author_2"),
                new Author(null, "Author_3")));

        genres = genreRepository.saveAll(List.of(
                new Genre(null, "Genre_1"),
                new Genre(null, "Genre_2"),
                new Genre(null, "Genre_3"),
                new Genre(null, "Genre_4")));

        books = bookRepository.saveAll(List.of(
                new Book(null, "BookTitle_1", authors.get(0), List.of(genres.get(0), genres.get(1))),
                new Book(null, "BookTitle_2", authors.get(1), List.of(genres.get(2), genres.get(3)))));
    }

    @DisplayName("должен загружать книгу по id вместе с автором и жанрами")
    @Test
    void shouldReturnCorrectBookById() {
        var expectedBook = books.get(0);

        var actualBook = bookService.findById(expectedBook.getId());

        assertThat(actualBook).isPresent();
        assertThat(actualBook.get().getAuthor()).isEqualTo(authors.get(0));
        assertThat(actualBook.get().getGenres()).containsExactly(genres.get(0), genres.get(1));
    }

    @DisplayName("должен возвращать пустой Optional, если книга не найдена")
    @Test
    void shouldReturnEmptyOptionalForUnknownId() {
        assertThat(bookService.findById("unknown-id")).isEmpty();
    }

    @DisplayName("должен загружать список всех книг вместе с авторами и жанрами, отсортированный по id")
    @Test
    void shouldReturnAllBooksSortedByIdWithRelations() {
        var expectedBooks = bookRepository.findAll().stream()
                .sorted(Comparator.comparing(Book::getId))
                .toList();

        var actualBooks = bookService.findAll();

        assertThat(actualBooks).containsExactlyElementsOf(expectedBooks);
        assertThat(actualBooks).extracting(Book::getAuthor).doesNotContainNull();
        assertThat(actualBooks).flatExtracting(Book::getGenres).doesNotContainNull();
    }

    @DisplayName("должен сохранять новую книгу с корректными ссылками на автора и жанры")
    @Test
    void shouldInsertNewBook() {
        var authorId = authors.get(2).getId();
        var genresIds = Set.of(genres.get(0).getId(), genres.get(2).getId());

        var savedBook = bookService.insert("New_Book", authorId, genresIds);

        assertThat(savedBook.getId()).isNotBlank();
        var bookFromDb = bookRepository.findById(savedBook.getId()).orElseThrow();
        assertThat(bookFromDb.getTitle()).isEqualTo("New_Book");
        assertThat(bookFromDb.getAuthor().getId()).isEqualTo(authorId);
        assertThat(bookFromDb.getGenres()).extracting(Genre::getId)
                .containsExactlyInAnyOrderElementsOf(genresIds);
    }

    @DisplayName("должен выбрасывать исключение при вставке книги с несуществующим автором")
    @Test
    void shouldThrowExceptionWhenInsertingWithNonExistentAuthor() {
        var genresIds = Set.of(genres.get(0).getId());

        assertThatThrownBy(() -> bookService.insert("New_Book", "unknown-id", genresIds))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @DisplayName("должен выбрасывать исключение при вставке книги с несуществующими жанрами")
    @Test
    void shouldThrowExceptionWhenInsertingWithNonExistentGenres() {
        var authorId = authors.get(0).getId();

        assertThatThrownBy(() -> bookService.insert("New_Book", authorId, Set.of("unknown-id")))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @DisplayName("должен обновлять книгу")
    @Test
    void shouldUpdateBook() {
        var book = books.get(0);
        var newAuthorId = authors.get(2).getId();
        var newGenresIds = Set.of(genres.get(2).getId(), genres.get(3).getId());

        var updatedBook = bookService.update(book.getId(), "Updated_Title", newAuthorId, newGenresIds);

        assertThat(updatedBook.getTitle()).isEqualTo("Updated_Title");
        var bookFromDb = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(bookFromDb.getTitle()).isEqualTo("Updated_Title");
        assertThat(bookFromDb.getAuthor().getId()).isEqualTo(newAuthorId);
        assertThat(bookFromDb.getGenres()).extracting(Genre::getId)
                .containsExactlyInAnyOrderElementsOf(newGenresIds);
    }

    @DisplayName("должен выбрасывать исключение при обновлении несуществующей книги")
    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentBook() {
        var genresIds = Set.of(genres.get(0).getId());

        assertThatThrownBy(() -> bookService.update("unknown-id", "Title", authors.get(0).getId(), genresIds))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @DisplayName("должен удалять книгу и каскадно удалять ее комментарии, не затрагивая комментарии других книг")
    @Test
    void shouldDeleteBookAndCascadeDeleteItsComments() {
        var bookToDelete = books.get(0);
        var otherBook = books.get(1);
        bookCommentRepository.saveAll(List.of(
                new BookComment("Comment_1", bookToDelete),
                new BookComment("Comment_2", bookToDelete),
                new BookComment("Comment_of_other_book", otherBook)));

        bookService.deleteById(bookToDelete.getId());

        assertThat(bookRepository.findById(bookToDelete.getId())).isEmpty();
        assertThat(bookCommentRepository.findAllByBookIdOrderByIdAsc(bookToDelete.getId())).isEmpty();
        assertThat(bookCommentRepository.findAllByBookIdOrderByIdAsc(otherBook.getId())).hasSize(1);
    }
}
