package ru.otus.hw.batch.importing;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.otus.hw.batch.mapping.EntityKind;
import ru.otus.hw.batch.mapping.IdMappingStore;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.otus.hw.batch.mapping.IdMappings.require;

@RequiredArgsConstructor
public class BookImportWriter implements ItemWriter<BookRow> {

    private final MongoImportSupport support;

    private final IdMappingStore<Long, String> idStore;

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public void write(Chunk<? extends BookRow> chunk) {
        var rows = chunk.getItems();
        var bookIds = support.assignIds(idStore, EntityKind.BOOK, rows.stream().map(BookRow::id).toList());
        var authorIds = idStore.getAll(EntityKind.AUTHOR,
                rows.stream().map(BookRow::authorId).filter(Objects::nonNull).distinct().toList());
        var genresByBook = genresByBook(rows);
        var books = rows.stream()
                .map(row -> new Book(bookIds.get(row.id()), row.title(), author(row, authorIds),
                        genresByBook.getOrDefault(row.id(), List.of())))
                .toList();
        support.upsertAll(Book.class, books, Book::getId);
    }

    private Author author(BookRow row, Map<Long, String> authorIds) {
        if (row.authorId() == null) {
            return null;
        }
        return new Author(require(authorIds, row.authorId(), EntityKind.AUTHOR), row.authorFullName());
    }

    private Map<Long, List<Genre>> genresByBook(List<? extends BookRow> rows) {
        var links = jdbc.query("""
                        select bg.book_id, g.id, g.name
                        from books_genres bg join genres g on g.id = bg.genre_id
                        where bg.book_id in (:ids)
                        order by bg.book_id, g.id""",
                Map.of("ids", rows.stream().map(BookRow::id).toList()),
                (rs, n) -> new BookGenreRow(rs.getLong(1), rs.getLong(2), rs.getString(3)));
        var genreIds = idStore.getAll(EntityKind.GENRE,
                links.stream().map(BookGenreRow::genreId).distinct().toList());
        return links.stream().collect(Collectors.groupingBy(BookGenreRow::bookId, Collectors.mapping(
                link -> new Genre(require(genreIds, link.genreId(), EntityKind.GENRE), link.genreName()),
                Collectors.toList())));
    }
}
