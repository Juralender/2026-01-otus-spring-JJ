package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;
import ru.otus.hw.services.dto.AuthorDto;
import ru.otus.hw.services.dto.BookCreateDto;
import ru.otus.hw.services.dto.BookDto;
import ru.otus.hw.services.dto.BookUpdateDto;
import ru.otus.hw.services.dto.GenreDto;

import java.util.List;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    @Transactional(readOnly = true)
    @Override
    public BookDto findById(long id) {
        return bookRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %d not found".formatted(id)));
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookDto> findAll() {
        return bookRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    @Override
    public BookDto insert(BookCreateDto bookCreateDto) {
        var author = getAuthor(bookCreateDto.getAuthorId());
        var genres = getGenres(bookCreateDto.getGenreIds());

        var book = new Book(0, bookCreateDto.getTitle(), author, genres);
        return toDto(bookRepository.save(book));
    }

    @Transactional
    @Override
    public BookDto update(BookUpdateDto bookUpdateDto) {
        var book = bookRepository.findById(bookUpdateDto.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Book with id %d not found".formatted(bookUpdateDto.getId())));

        var author = getAuthor(bookUpdateDto.getAuthorId());
        var genres = getGenres(bookUpdateDto.getGenreIds());

        book.setTitle(bookUpdateDto.getTitle());
        book.setAuthor(author);
        book.setGenres(genres);

        return toDto(bookRepository.save(book));
    }

    @Transactional
    @Override
    public void deleteById(long id) {
        bookRepository.deleteById(id);
    }

    private Author getAuthor(long authorId) {
        return authorRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Author with id %d not found".formatted(authorId)));
    }

    private List<Genre> getGenres(Set<Long> genresIds) {
        if (isEmpty(genresIds)) {
            throw new IllegalArgumentException("Genres ids must not be null");
        }

        var genres = genreRepository.findAllByIdIn(genresIds);
        if (isEmpty(genres) || genresIds.size() != genres.size()) {
            throw new EntityNotFoundException("One or all genres with ids %s not found".formatted(genresIds));
        }
        return genres;
    }

    private BookDto toDto(Book book) {
        var authorDto = new AuthorDto(book.getAuthor().getId(), book.getAuthor().getFullName());
        var genreDtos = book.getGenres().stream()
                .map(genre -> new GenreDto(genre.getId(), genre.getName()))
                .toList();
        return new BookDto(book.getId(), book.getTitle(), authorDto, genreDtos);
    }
}
