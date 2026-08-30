package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import reactor.test.StepVerifier;
import ru.otus.hw.models.BookComment;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе R2DBC для работы с комментариями к книгам")
@DataR2dbcTest
class BookCommentRepositoryTest {

    @Autowired
    private BookCommentRepository repository;

    @DisplayName("должен загружать комментарий по id")
    @Test
    void shouldReturnCorrectCommentById() {
        StepVerifier.create(repository.save(new BookComment("Comment_1", 1L))
                        .flatMap(saved -> repository.findById(saved.getId())))
                .assertNext(actual -> {
                    assertThat(actual.getText()).isEqualTo("Comment_1");
                    assertThat(actual.getBookId()).isEqualTo(1L);
                })
                .verifyComplete();
    }

    @DisplayName("должен возвращать пустой Mono, если комментарий не найден")
    @Test
    void shouldReturnEmptyMonoForUnknownId() {
        StepVerifier.create(repository.findById(10_000L))
                .verifyComplete();
    }

    @DisplayName("должен загружать все комментарии книги по ее id")
    @Test
    void shouldReturnAllCommentsForBook() {
        var setup = repository.save(new BookComment("Comment_3_1", 3L))
                .then(repository.save(new BookComment("Comment_3_2", 3L)))
                .then(repository.save(new BookComment("Comment_2_1", 2L)));

        StepVerifier.create(setup.thenMany(repository.findAllByBookIdOrderByIdAsc(3L)))
                .assertNext(comment -> assertThat(comment.getText()).isEqualTo("Comment_3_1"))
                .assertNext(comment -> assertThat(comment.getText()).isEqualTo("Comment_3_2"))
                .verifyComplete();
    }

    @DisplayName("должен сохранять новый комментарий")
    @Test
    void shouldSaveNewComment() {
        var newComment = new BookComment("New comment", 1L);

        StepVerifier.create(repository.save(newComment))
                .assertNext(actual -> {
                    assertThat(actual.getId()).isGreaterThan(0);
                    assertThat(actual.getText()).isEqualTo("New comment");
                    assertThat(actual.getBookId()).isEqualTo(1L);
                })
                .verifyComplete();
    }

    @DisplayName("должен сохранять измененный комментарий")
    @Test
    void shouldSaveUpdatedComment() {
        StepVerifier.create(repository.save(new BookComment("Old text", 1L))
                        .flatMap(comment -> {
                            comment.setText("New text");
                            return repository.save(comment);
                        })
                        .flatMap(saved -> repository.findById(saved.getId())))
                .assertNext(actual -> assertThat(actual.getText()).isEqualTo("New text"))
                .verifyComplete();
    }

    @DisplayName("должен удалять комментарий по id")
    @Test
    void shouldDeleteComment() {
        StepVerifier.create(repository.save(new BookComment("To delete", 1L))
                        .flatMap(saved -> repository.deleteById(saved.getId()).thenReturn(saved))
                        .flatMap(saved -> repository.findById(saved.getId())))
                .verifyComplete();
    }
}
