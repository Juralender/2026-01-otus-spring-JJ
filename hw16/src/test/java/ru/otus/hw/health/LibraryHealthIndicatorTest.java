package ru.otus.hw.health;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Status;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@DisplayName("Индикатор здоровья библиотеки")
@ExtendWith(MockitoExtension.class)
class LibraryHealthIndicatorTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private LibraryHealthIndicator indicator;

    @DisplayName("должен возвращать UP и количество записей, если книги есть")
    @Test
    void shouldReturnUpWhenBooksExist() {
        given(bookRepository.count()).willReturn(3L);
        given(authorRepository.count()).willReturn(2L);
        given(genreRepository.count()).willReturn(6L);

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails())
                .containsEntry("books", 3L)
                .containsEntry("authors", 2L)
                .containsEntry("genres", 6L);
    }

    @DisplayName("должен возвращать DOWN, если книг нет")
    @Test
    void shouldReturnDownWhenNoBooks() {
        given(bookRepository.count()).willReturn(0L);

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails())
                .containsEntry("books", 0L)
                .containsEntry("reason", "Library has no books");
    }

    @DisplayName("должен возвращать DOWN, если база недоступна")
    @Test
    void shouldReturnDownWhenRepositoryFails() {
        given(bookRepository.count()).willThrow(new IllegalStateException("DB is unavailable"));

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails())
                .containsEntry("error", "java.lang.IllegalStateException: DB is unavailable");
    }
}
