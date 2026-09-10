package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.dto.AuthorDto;
import ru.otus.hw.services.dto.BookCreateDto;
import ru.otus.hw.services.dto.BookUpdateDto;
import ru.otus.hw.services.dto.GenreDto;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Интеграционные тесты сервиса книг")
@DataJpaTest
@Import(BookServiceImpl.class)
@Transactional(propagation = Propagation.NEVER)
class BookServiceIntegrationTest {

    @Autowired
    private BookService bookService;

    @DisplayName("должен возвращать автора и жанры книги в виде DTO (findById)")
    @Test
    void shouldReturnAuthorAndGenresDtoOnFindById() {
        var book = bookService.findById(1L);

        assertThat(book.getAuthor()).isEqualTo(new AuthorDto(1L, "Author_1"));
        assertThat(book.getGenres()).extracting(GenreDto::getName)
                .containsExactly("Genre_1", "Genre_2");
    }

    @DisplayName("должен возвращать автора и жанры каждой книги в виде DTO (findAll)")
    @Test
    void shouldReturnAuthorAndGenresDtoOnFindAll() {
        var books = bookService.findAll();

        assertThat(books).isNotEmpty();
        assertThatCode(() -> books.forEach(book -> {
            book.getAuthor().getFullName();
            book.getGenres().forEach(GenreDto::getName);
        })).doesNotThrowAnyException();
    }

    @DisplayName("должен возвращать автора и жанры только что добавленной книги в виде DTO")
    @Test
    void shouldReturnAuthorAndGenresDtoAfterInsert() {
        var savedBook = bookService.insert(new BookCreateDto("Integration_Test_Book", 1L, Set.of(1L, 2L)));
        try {
            assertThat(savedBook.getAuthor()).isEqualTo(new AuthorDto(1L, "Author_1"));
            assertThat(savedBook.getGenres()).extracting(GenreDto::getName)
                    .containsExactlyInAnyOrder("Genre_1", "Genre_2");
        } finally {
            bookService.deleteById(savedBook.getId());
        }
    }

    @DisplayName("должен возвращать автора и жанры обновленной книги в виде DTO")
    @Test
    void shouldReturnAuthorAndGenresDtoAfterUpdate() {
        var savedBook = bookService.insert(new BookCreateDto("Integration_Test_Book_2", 1L, Set.of(1L)));
        try {
            var updatedBook = bookService.update(
                    new BookUpdateDto(savedBook.getId(), "Updated_Title", 2L, Set.of(3L, 4L)));

            assertThat(updatedBook.getAuthor()).isEqualTo(new AuthorDto(2L, "Author_2"));
            assertThat(updatedBook.getGenres()).extracting(GenreDto::getName)
                    .containsExactlyInAnyOrder("Genre_3", "Genre_4");
        } finally {
            bookService.deleteById(savedBook.getId());
        }
    }

    @DisplayName("должен выбрасывать исключение при обновлении несуществующей книги")
    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentBook() {
        assertThatThrownBy(() -> bookService.update(new BookUpdateDto(10_000L, "Title", 1L, Set.of(1L))))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @DisplayName("должен удалять книгу")
    @Test
    void shouldDeleteBook() {
        var savedBook = bookService.insert(new BookCreateDto("Integration_Test_Book_3", 1L, Set.of(1L)));

        bookService.deleteById(savedBook.getId());

        assertThatThrownBy(() -> bookService.findById(savedBook.getId()))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
