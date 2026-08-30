package ru.otus.hw.rest.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookGenreRelation;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookGenreRelationRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;
import ru.otus.hw.services.dto.AuthorDto;
import ru.otus.hw.services.dto.BookCreateDto;
import ru.otus.hw.services.dto.BookDto;
import ru.otus.hw.services.dto.BookUpdateDto;
import ru.otus.hw.services.dto.GenreDto;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookRestController {

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    private final BookGenreRelationRepository bookGenreRelationRepository;

    @Transactional(readOnly = true)
    @GetMapping
    public Flux<BookDto> findAll() {
        return bookRepository.findAll().collectList().flatMapMany(this::toDtos);
    }

    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    public Mono<BookDto> findById(@PathVariable long id) {
        return bookRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Book with id %d not found".formatted(id))))
                .flatMap(book -> toDtos(List.of(book)).next());
    }

    @Transactional
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<BookDto> create(@Valid @RequestBody BookCreateDto bookCreateDto) {
        return Mono.zip(getAuthor(bookCreateDto.getAuthorId()), getGenres(bookCreateDto.getGenreIds()))
                .flatMap(tuple -> bookRepository.save(new Book(0, bookCreateDto.getTitle(), tuple.getT1().getId())))
                .flatMap(book -> bookGenreRelationRepository.saveAll(book.getId(), bookCreateDto.getGenreIds())
                        .then(Mono.just(book)))
                .flatMap(book -> toDtos(List.of(book)).next());
    }

    @Transactional
    @PutMapping("/{id}")
    public Mono<BookDto> update(@PathVariable long id, @Valid @RequestBody BookUpdateDto bookUpdateDto) {
        return bookRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Book with id %d not found".formatted(id))))
                .flatMap(book -> Mono.zip(getAuthor(bookUpdateDto.getAuthorId()), getGenres(bookUpdateDto.getGenreIds()))
                        .flatMap(tuple -> {
                            book.setTitle(bookUpdateDto.getTitle());
                            book.setAuthorId(tuple.getT1().getId());
                            return bookRepository.save(book);
                        }))
                .flatMap(book -> bookGenreRelationRepository.deleteAllByBookId(book.getId())
                        .thenMany(bookGenreRelationRepository.saveAll(book.getId(), bookUpdateDto.getGenreIds()))
                        .then(Mono.just(book)))
                .flatMap(book -> toDtos(List.of(book)).next());
    }

    @Transactional
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable long id) {
        return bookRepository.deleteById(id);
    }

    private Mono<Author> getAuthor(long authorId) {
        return authorRepository.findById(authorId)
                .switchIfEmpty(Mono.error(
                        new EntityNotFoundException("Author with id %d not found".formatted(authorId))));
    }

    private Mono<List<Genre>> getGenres(Set<Long> genreIds) {
        return genreRepository.findAllByIdIn(genreIds)
                .collectList()
                .flatMap(genres -> isEmpty(genres) || genreIds.size() != genres.size()
                        ? Mono.error(new EntityNotFoundException(
                                "One or all genres with ids %s not found".formatted(genreIds)))
                        : Mono.just(genres));
    }

    private Flux<BookDto> toDtos(List<Book> books) {
        if (books.isEmpty()) {
            return Flux.empty();
        }

        var authorIds = books.stream().map(Book::getAuthorId).collect(Collectors.toSet());
        var bookIds = books.stream().map(Book::getId).toList();

        var authorsByIdMono = authorRepository.findAllByIdIn(authorIds)
                .collectMap(Author::getId, Function.identity());
        var genreIdsByBookMono = bookGenreRelationRepository.findAllByBookIdIn(bookIds)
                .collectMultimap(BookGenreRelation::getBookId, BookGenreRelation::getGenreId);

        return Mono.zip(authorsByIdMono, genreIdsByBookMono)
                .flatMapMany(tuple -> toDtos(books, tuple.getT1(), tuple.getT2()));
    }

    private Flux<BookDto> toDtos(List<Book> books, Map<Long, Author> authorsById,
                                  Map<Long, Collection<Long>> genreIdsByBook) {
        var genreIds = genreIdsByBook.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        return genreRepository.findAllByIdIn(genreIds)
                .collectMap(Genre::getId, Function.identity())
                .flatMapMany(genresById -> Flux.fromIterable(books)
                        .map(book -> toDto(book, authorsById, genreIdsByBook, genresById)));
    }

    private BookDto toDto(Book book, Map<Long, Author> authorsById, Map<Long, Collection<Long>> genreIdsByBook,
                           Map<Long, Genre> genresById) {
        var author = authorsById.get(book.getAuthorId());
        var authorDto = new AuthorDto(author.getId(), author.getFullName());
        var genreDtos = genreIdsByBook.getOrDefault(book.getId(), List.of()).stream()
                .map(genresById::get)
                .sorted(Comparator.comparingLong(Genre::getId))
                .map(genre -> new GenreDto(genre.getId(), genre.getName()))
                .toList();
        return new BookDto(book.getId(), book.getTitle(), authorDto, genreDtos);
    }
}
