package ru.otus.hw.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;

@RequiredArgsConstructor
@Component
public class DemoDataInitializer implements CommandLineRunner {

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    @Override
    public void run(String... args) {
        if (authorRepository.count() > 0 || genreRepository.count() > 0 || bookRepository.count() > 0) {
            return;
        }

        var authors = authorRepository.saveAll(List.of(
                new Author(null, "Author_1"),
                new Author(null, "Author_2"),
                new Author(null, "Author_3")));

        var genres = genreRepository.saveAll(List.of(
                new Genre(null, "Genre_1"),
                new Genre(null, "Genre_2"),
                new Genre(null, "Genre_3"),
                new Genre(null, "Genre_4"),
                new Genre(null, "Genre_5"),
                new Genre(null, "Genre_6")));

        bookRepository.saveAll(List.of(
                new Book(null, "BookTitle_1", authors.get(0), List.of(genres.get(0), genres.get(1))),
                new Book(null, "BookTitle_2", authors.get(1), List.of(genres.get(2), genres.get(3))),
                new Book(null, "BookTitle_3", authors.get(2), List.of(genres.get(4), genres.get(5)))));
    }
}
