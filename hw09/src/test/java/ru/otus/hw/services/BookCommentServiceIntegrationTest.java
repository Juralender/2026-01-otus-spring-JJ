package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.dto.BookCommentCreateDto;
import ru.otus.hw.services.dto.BookCommentDto;
import ru.otus.hw.services.dto.BookCommentUpdateDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Интеграционные тесты сервиса комментариев к книгам")
@DataJpaTest
@Import(BookCommentServiceImpl.class)
@Transactional(propagation = Propagation.NEVER)
class BookCommentServiceIntegrationTest {

    @Autowired
    private BookCommentService bookCommentService;

    @DisplayName("должен возвращать id книги комментария при добавлении")
    @Test
    void shouldReturnBookIdAfterInsert() {
        var comment = bookCommentService.insert(new BookCommentCreateDto("Integration test comment", 1L));
        try {
            assertThat(comment.getBookId()).isEqualTo(1L);
        } finally {
            bookCommentService.deleteById(comment.getId());
        }
    }

    @DisplayName("должен возвращать id книги комментария (findById)")
    @Test
    void shouldReturnBookIdOnFindById() {
        var inserted = bookCommentService.insert(new BookCommentCreateDto("Integration test comment 2", 2L));
        try {
            var found = bookCommentService.findById(inserted.getId());

            assertThat(found.getBookId()).isEqualTo(2L);
        } finally {
            bookCommentService.deleteById(inserted.getId());
        }
    }

    @DisplayName("должен возвращать id книги каждого комментария (findAllByBookId)")
    @Test
    void shouldReturnBookIdForEachCommentOnFindAllByBookId() {
        var first = bookCommentService.insert(new BookCommentCreateDto("Comment A", 3L));
        var second = bookCommentService.insert(new BookCommentCreateDto("Comment B", 3L));
        try {
            var comments = bookCommentService.findAllByBookId(3L);

            assertThat(comments).extracting(BookCommentDto::getText).contains("Comment A", "Comment B");
            assertThat(comments).extracting(BookCommentDto::getBookId).containsOnly(3L);
        } finally {
            bookCommentService.deleteById(first.getId());
            bookCommentService.deleteById(second.getId());
        }
    }

    @DisplayName("должен обновлять текст комментария")
    @Test
    void shouldUpdateComment() {
        var inserted = bookCommentService.insert(new BookCommentCreateDto("Old text", 1L));
        try {
            var updated = bookCommentService.update(new BookCommentUpdateDto(inserted.getId(), "New text"));

            assertThat(updated.getText()).isEqualTo("New text");
            assertThat(updated.getBookId()).isEqualTo(1L);
        } finally {
            bookCommentService.deleteById(inserted.getId());
        }
    }

    @DisplayName("должен выбрасывать исключение при обновлении несуществующего комментария")
    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentComment() {
        assertThatThrownBy(() -> bookCommentService.update(new BookCommentUpdateDto(10_000L, "New text")))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @DisplayName("должен удалять комментарий")
    @Test
    void shouldDeleteComment() {
        var inserted = bookCommentService.insert(new BookCommentCreateDto("To delete", 1L));

        bookCommentService.deleteById(inserted.getId());

        assertThatThrownBy(() -> bookCommentService.findById(inserted.getId()))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
