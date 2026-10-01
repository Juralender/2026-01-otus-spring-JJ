package ru.otus.hw.batch.exporting;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import ru.otus.hw.batch.mapping.EntityKind;
import ru.otus.hw.batch.mapping.IdMappingStore;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static ru.otus.hw.batch.mapping.IdMappings.require;

@RequiredArgsConstructor
public class BookExportWriter implements ItemWriter<Book> {

    private final JdbcBatchInserter inserter;

    private final IdMappingStore<String, Long> idStore;

    @Override
    public void write(Chunk<? extends Book> chunk) {
        var books = chunk.getItems();
        var authorIds = idStore.getAll(EntityKind.AUTHOR, books.stream()
                .map(Book::getAuthor).filter(Objects::nonNull).map(author -> author.getId()).distinct().toList());
        var bookIds = inserter.<Book>insert("insert into books (title, author_id) values (:title, :authorId)",
                books, Book::getId, book -> new MapSqlParameterSource()
                        .addValue("title", book.getTitle())
                        .addValue("authorId", book.getAuthor() == null ? null
                                : require(authorIds, book.getAuthor().getId(), EntityKind.AUTHOR)));
        idStore.putAll(EntityKind.BOOK, bookIds);
        insertGenreLinks(books, bookIds);
    }

    private void insertGenreLinks(List<? extends Book> books, Map<String, Long> bookIds) {
        var genreIds = idStore.getAll(EntityKind.GENRE, books.stream()
                .flatMap(book -> genresOf(book).stream()).map(Genre::getId).distinct().toList());
        var links = new ArrayList<SqlParameterSource>();
        for (Book book : books) {
            for (Genre genre : genresOf(book)) {
                links.add(new MapSqlParameterSource()
                        .addValue("bookId", bookIds.get(book.getId()))
                        .addValue("genreId", require(genreIds, genre.getId(), EntityKind.GENRE)));
            }
        }
        inserter.insert("insert into books_genres (book_id, genre_id) values (:bookId, :genreId)", links);
    }

    private static List<Genre> genresOf(Book book) {
        return book.getGenres() == null ? List.of() : book.getGenres();
    }
}
