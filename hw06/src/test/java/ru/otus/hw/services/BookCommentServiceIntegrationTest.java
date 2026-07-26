package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.otus.hw.models.BookComment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("Интеграционные тесты сервиса комментариев к книгам")
@SpringBootTest
class BookCommentServiceIntegrationTest {

    @Autowired
    private BookCommentService bookCommentService;

    @DisplayName("должен позволять обращаться к книге комментария вне транзакции сервиса (insert)")
    @Test
    void shouldAllowAccessingBookAfterInsertOutsideTransaction() {
        var comment = bookCommentService.insert("Integration test comment", 1L);
        try {
            assertThatCode(() -> assertThat(comment.getBook().getTitle()).isEqualTo("BookTitle_1"))
                    .doesNotThrowAnyException();
        } finally {
            bookCommentService.deleteById(comment.getId());
        }
    }

    @DisplayName("должен позволять обращаться к книге комментария вне транзакции сервиса (findById)")
    @Test
    void shouldAllowAccessingBookAfterFindByIdOutsideTransaction() {
        var inserted = bookCommentService.insert("Integration test comment 2", 2L);
        try {
            var found = bookCommentService.findById(inserted.getId()).orElseThrow();

            assertThatCode(() -> assertThat(found.getBook().getTitle()).isEqualTo("BookTitle_2"))
                    .doesNotThrowAnyException();
        } finally {
            bookCommentService.deleteById(inserted.getId());
        }
    }

    @DisplayName("должен позволять обращаться к книге каждого комментария вне транзакции сервиса (findAllByBookId)")
    @Test
    void shouldAllowAccessingBookAfterFindAllByBookIdOutsideTransaction() {
        var first = bookCommentService.insert("Comment A", 3L);
        var second = bookCommentService.insert("Comment B", 3L);
        try {
            var comments = bookCommentService.findAllByBookId(3L);

            assertThat(comments).extracting(BookComment::getText).contains("Comment A", "Comment B");
            assertThatCode(() -> comments.forEach(c -> c.getBook().getTitle()))
                    .doesNotThrowAnyException();
        } finally {
            bookCommentService.deleteById(first.getId());
            bookCommentService.deleteById(second.getId());
        }
    }

    @DisplayName("должен обновлять текст комментария")
    @Test
    void shouldUpdateComment() {
        var inserted = bookCommentService.insert("Old text", 1L);
        try {
            var updated = bookCommentService.update(inserted.getId(), "New text");

            assertThat(updated.getText()).isEqualTo("New text");
            assertThatCode(() -> updated.getBook().getTitle()).doesNotThrowAnyException();
        } finally {
            bookCommentService.deleteById(inserted.getId());
        }
    }

    @DisplayName("должен удалять комментарий")
    @Test
    void shouldDeleteComment() {
        var inserted = bookCommentService.insert("To delete", 1L);

        bookCommentService.deleteById(inserted.getId());

        assertThat(bookCommentService.findById(inserted.getId())).isEmpty();
    }
}
