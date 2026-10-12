package ru.otus.hw.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

@Component("library")
@RequiredArgsConstructor
public class LibraryHealthIndicator implements HealthIndicator {

    private final BookRepository bookRepository;

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    @Override
    public Health health() {
        try {
            long books = bookRepository.count();
            var builder = books > 0
                    ? Health.up()
                    : Health.down().withDetail("reason", "Library has no books");
            return builder
                    .withDetail("books", books)
                    .withDetail("authors", authorRepository.count())
                    .withDetail("genres", genreRepository.count())
                    .build();
        } catch (RuntimeException e) {
            return Health.down(e).build();
        }
    }
}
