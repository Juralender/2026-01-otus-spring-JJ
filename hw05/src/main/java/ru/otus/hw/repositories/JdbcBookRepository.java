package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JdbcBookRepository implements BookRepository {

    private final GenreRepository genreRepository;

    private final NamedParameterJdbcOperations namedParameterJdbcOperations;

    @Override
    public Optional<Book> findById(long id) {
        String sql = "select book.id as id, book.title as title, book.author_id as author_id, author.full_name as full_name, "
                + "genre.id as genre_id, genre.name as genre_name "
                + "from books book "
                + "join authors author on author.id = book.author_id "
                + "left join books_genres bg on bg.book_id = book.id "
                + "left join genres genre on genre.id = bg.genre_id "
                + "where book.id = :id";
        var params = new MapSqlParameterSource("id", id);
        return Optional.ofNullable(
                namedParameterJdbcOperations.query(sql, params, new BookResultSetExtractor()));
    }

    @Override
    public List<Book> findAll() {
        var genres = genreRepository.findAll();
        var books = getAllBooksWithoutGenres();
        var relations = getAllGenreRelations();
        mergeBooksInfo(books, genres, relations);
        return books;
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            return insert(book);
        }
        return update(book);
    }

    @Override
    public void deleteById(long id) {
        var params = new MapSqlParameterSource("id", id);
        namedParameterJdbcOperations.update("delete from books where id = :id", params);
    }

    private List<Book> getAllBooksWithoutGenres() {
        String sql = "select book.id as id, book.title as title, book.author_id as author_id, author.full_name as full_name "
                + "from books book join authors author on author.id = book.author_id";
        return namedParameterJdbcOperations.query(sql, new BookRowMapper());
    }

    private List<BookGenreRelation> getAllGenreRelations() {
        String sql = "select book_id, genre_id from books_genres";
        return namedParameterJdbcOperations.query(sql,
                (rs, rowNum) -> new BookGenreRelation(rs.getLong("book_id"), rs.getLong("genre_id")));
    }

    private void mergeBooksInfo(List<Book> booksWithoutGenres, List<Genre> genres,
                                List<BookGenreRelation> relations) {
        var booksById = booksWithoutGenres.stream().collect(Collectors.toMap(Book::getId, Function.identity()));
        var genresById = genres.stream().collect(Collectors.toMap(Genre::getId, Function.identity()));
        relations.forEach(relation -> {
            var book = booksById.get(relation.bookId());
            var genre = genresById.get(relation.genreId());
            if (book != null && genre != null) {
                book.getGenres().add(genre);
            }
        });
    }

    private Book insert(Book book) {
        var keyHolder = new GeneratedKeyHolder();

        var params = new MapSqlParameterSource()
                .addValue("title", book.getTitle())
                .addValue("author_id", book.getAuthor().getId());
        namedParameterJdbcOperations.update(
                "insert into books (title, author_id) values (:title, :author_id)",
                params, keyHolder, new String[]{"id"});

        //noinspection DataFlowIssue
        book.setId(keyHolder.getKeyAs(Long.class));
        batchInsertGenresRelationsFor(book);
        return book;
    }

    private Book update(Book book) {
        var params = new MapSqlParameterSource()
                .addValue("id", book.getId())
                .addValue("title", book.getTitle())
                .addValue("author_id", book.getAuthor().getId());
        int updatedRows = namedParameterJdbcOperations.update(
                "update books set title = :title, author_id = :author_id where id = :id", params);

        if (updatedRows == 0) {
            throw new EntityNotFoundException("Book with id %d not found".formatted(book.getId()));
        }

        removeGenresRelationsFor(book);
        batchInsertGenresRelationsFor(book);

        return book;
    }

    private void batchInsertGenresRelationsFor(Book book) {
        var batchParams = book.getGenres().stream()
                .map(genre -> new MapSqlParameterSource()
                        .addValue("book_id", book.getId())
                        .addValue("genre_id", genre.getId()))
                .toArray(SqlParameterSource[]::new);
        namedParameterJdbcOperations.batchUpdate(
                "insert into books_genres (book_id, genre_id) values (:book_id, :genre_id)", batchParams);
    }

    private void removeGenresRelationsFor(Book book) {
        var params = new MapSqlParameterSource("book_id", book.getId());
        namedParameterJdbcOperations.update("delete from books_genres where book_id = :book_id", params);
    }

    private static class BookRowMapper implements RowMapper<Book> {

        @Override
        public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
            long id = rs.getLong("id");
            String title = rs.getString("title");
            var author = new Author(rs.getLong("author_id"), rs.getString("full_name"));
            return new Book(id, title, author, new ArrayList<>());
        }
    }

    // Использовать для findById
    @SuppressWarnings("ClassCanBeRecord")
    @RequiredArgsConstructor
    private static class BookResultSetExtractor implements ResultSetExtractor<Book> {

        @Override
        public Book extractData(ResultSet rs) throws SQLException, DataAccessException {
            Book book = null;
            List<Genre> genres = new ArrayList<>();
            while (rs.next()) {
                if (book == null) {
                    var author = new Author(rs.getLong("author_id"), rs.getString("full_name"));
                    book = new Book(rs.getLong("id"), rs.getString("title"), author, genres);
                }
                genres.add(new Genre(rs.getLong("genre_id"), rs.getString("genre_name")));
            }
            return book;
        }
    }

    private record BookGenreRelation(long bookId, long genreId) {
    }
}
