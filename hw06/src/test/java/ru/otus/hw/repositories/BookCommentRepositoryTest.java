package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Репозиторий на основе Jpa для работы с комментариями к книгам")
@DataJpaTest
@Import(JpaBookCommentRepository.class)
class BookCommentRepositoryTest {

    @Autowired
    private JpaBookCommentRepository repository;

    @Autowired
    private TestEntityManager testEntityManager;

    @DisplayName("должен загружать комментарий по id вместе с книгой")
    @Test
    void shouldReturnCorrectCommentById() {
        var book = testEntityManager.find(Book.class, 1L);
        var comment = testEntityManager.persistAndFlush(new BookComment("Comment_1", book));

        var actualComment = repository.findById(comment.getId());

        assertThat(actualComment).isPresent();
        assertThat(actualComment.get().getText()).isEqualTo("Comment_1");
        assertThat(actualComment.get().getBook().getId()).isEqualTo(book.getId());
    }

    @DisplayName("должен возвращать пустой Optional, если комментарий не найден")
    @Test
    void shouldReturnEmptyOptionalForUnknownId() {
        assertThat(repository.findById(10_000L)).isEmpty();
    }

    @DisplayName("должен загружать все комментарии книги по ее id")
    @Test
    void shouldReturnAllCommentsForBook() {
        var book1 = testEntityManager.find(Book.class, 1L);
        var book2 = testEntityManager.find(Book.class, 2L);
        testEntityManager.persistAndFlush(new BookComment("Comment_1_1", book1));
        testEntityManager.persistAndFlush(new BookComment("Comment_1_2", book1));
        testEntityManager.persistAndFlush(new BookComment("Comment_2_1", book2));

        var actualComments = repository.findAllByBookId(book1.getId());

        assertThat(actualComments).extracting(BookComment::getText)
                .containsExactly("Comment_1_1", "Comment_1_2");
    }

    @DisplayName("должен сохранять новый комментарий")
    @Test
    void shouldSaveNewComment() {
        var book = testEntityManager.find(Book.class, 1L);
        var newComment = new BookComment("New comment", book);

        var savedComment = repository.save(newComment);
        testEntityManager.flush();
        testEntityManager.clear();

        assertThat(savedComment.getId()).isGreaterThan(0);

        var commentFromDb = testEntityManager.find(BookComment.class, savedComment.getId());
        assertThat(commentFromDb).isNotNull();
        assertThat(commentFromDb.getText()).isEqualTo("New comment");
        assertThat(commentFromDb.getBook().getId()).isEqualTo(book.getId());
    }

    @DisplayName("должен сохранять измененный комментарий")
    @Test
    void shouldSaveUpdatedComment() {
        var book = testEntityManager.find(Book.class, 1L);
        var comment = testEntityManager.persistAndFlush(new BookComment("Old text", book));
        var updatedComment = new BookComment(comment.getId(), "New text", book);

        repository.save(updatedComment);
        testEntityManager.flush();
        testEntityManager.clear();

        var commentFromDb = testEntityManager.find(BookComment.class, comment.getId());
        assertThat(commentFromDb.getText()).isEqualTo("New text");
    }

    @DisplayName("должен выбрасывать исключение при обновлении несуществующего комментария")
    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentComment() {
        var book = testEntityManager.find(Book.class, 1L);
        var missingComment = new BookComment(10_000L, "text", book);

        assertThatThrownBy(() -> repository.save(missingComment))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @DisplayName("должен удалять комментарий по id")
    @Test
    void shouldDeleteComment() {
        var book = testEntityManager.find(Book.class, 1L);
        var comment = testEntityManager.persistAndFlush(new BookComment("To delete", book));

        repository.deleteById(comment.getId());
        testEntityManager.flush();
        testEntityManager.clear();

        assertThat(testEntityManager.find(BookComment.class, comment.getId())).isNull();
    }
}
