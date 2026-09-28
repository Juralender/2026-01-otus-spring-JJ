package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookCommentRepository;
import ru.otus.hw.repositories.BookRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Интеграционные тесты сервиса комментариев к книгам")
@DataMongoTest
@Import(BookCommentServiceImpl.class)
class BookCommentServiceIntegrationTest {

    @Autowired
    private BookCommentService bookCommentService;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookCommentRepository bookCommentRepository;

    private List<Book> books;

    @BeforeEach
    void setUp() {
        bookCommentRepository.deleteAll();
        bookRepository.deleteAll();
        authorRepository.deleteAll();

        var author = authorRepository.save(new Author(null, "Author_1"));
        books = bookRepository.saveAll(List.of(
                new Book(null, "BookTitle_1", author, List.of()),
                new Book(null, "BookTitle_2", author, List.of())));
    }

    @DisplayName("должен сохранять новый комментарий со ссылкой на книгу")
    @Test
    void shouldInsertNewCommentWithBookReference() {
        var book = books.get(0);

        var savedComment = bookCommentService.insert("New comment", book.getId());

        assertThat(savedComment.getId()).isNotBlank();
        var commentFromDb = bookCommentRepository.findById(savedComment.getId()).orElseThrow();
        assertThat(commentFromDb.getText()).isEqualTo("New comment");
        assertThat(commentFromDb.getBook().getId()).isEqualTo(book.getId());
    }

    @DisplayName("должен выбрасывать исключение при вставке комментария для несуществующей книги")
    @Test
    void shouldThrowExceptionWhenInsertingForNonExistentBook() {
        assertThatThrownBy(() -> bookCommentService.insert("text", "unknown-id"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @DisplayName("должен загружать комментарий по id вместе с книгой")
    @Test
    void shouldReturnCorrectCommentById() {
        var book = books.get(0);
        var comment = bookCommentRepository.save(new BookComment("Comment_1", book));

        var actualComment = bookCommentService.findById(comment.getId());

        assertThat(actualComment).isPresent();
        assertThat(actualComment.get().getText()).isEqualTo("Comment_1");
        assertThat(actualComment.get().getBook().getId()).isEqualTo(book.getId());
    }

    @DisplayName("должен возвращать пустой Optional, если комментарий не найден")
    @Test
    void shouldReturnEmptyOptionalForUnknownId() {
        assertThat(bookCommentService.findById("unknown-id")).isEmpty();
    }

    @DisplayName("должен загружать все комментарии книги по ее id в порядке добавления")
    @Test
    void shouldReturnAllCommentsForBookOrderedById() {
        var book1 = books.get(0);
        var book2 = books.get(1);
        bookCommentRepository.saveAll(List.of(
                new BookComment("Comment_1_1", book1),
                new BookComment("Comment_1_2", book1),
                new BookComment("Comment_2_1", book2)));

        var actualComments = bookCommentService.findAllByBookId(book1.getId());

        assertThat(actualComments).extracting(BookComment::getText)
                .containsExactly("Comment_1_1", "Comment_1_2");
    }

    @DisplayName("должен обновлять и сохранять текст комментария")
    @Test
    void shouldUpdateAndPersistComment() {
        var comment = bookCommentRepository.save(new BookComment("Old text", books.get(0)));

        var updatedComment = bookCommentService.update(comment.getId(), "New text");

        assertThat(updatedComment.getText()).isEqualTo("New text");
        var commentFromDb = bookCommentRepository.findById(comment.getId()).orElseThrow();
        assertThat(commentFromDb.getText()).isEqualTo("New text");
    }

    @DisplayName("должен выбрасывать исключение при обновлении несуществующего комментария")
    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentComment() {
        assertThatThrownBy(() -> bookCommentService.update("unknown-id", "New text"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @DisplayName("должен удалять комментарий по id")
    @Test
    void shouldDeleteComment() {
        var comment = bookCommentRepository.save(new BookComment("To delete", books.get(0)));

        bookCommentService.deleteById(comment.getId());

        assertThat(bookCommentRepository.findById(comment.getId())).isEmpty();
    }
}
