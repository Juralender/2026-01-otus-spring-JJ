package ru.otus.hw.batch;

import com.mongodb.DBRef;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.models.Genre;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class MigrationTestData {

    public static final int AUTHORS = 7;

    public static final int GENRES = 5;

    public static final int BOOKS = 45;

    public static final int COMMENTS_PER_BOOK = 2;

    private final JdbcTemplate jdbcTemplate;

    private final MongoTemplate mongoTemplate;

    public void clearSql() {
        List.of("book_comments", "books_genres", "books", "authors", "genres")
                .forEach(table -> jdbcTemplate.update("delete from " + table));
    }

    public void clearMongo() {
        List.of(BookComment.class, Book.class, Author.class, Genre.class)
                .forEach(type -> mongoTemplate.remove(new Query(), type));
    }

    public void seedMongo() {
        clearMongo();
        var authors = mongoTemplate.insertAll(IntStream.range(0, AUTHORS)
                .mapToObj(i -> new Author(null, "Author_" + i)).toList()).stream().toList();
        var genres = mongoTemplate.insertAll(IntStream.range(0, GENRES)
                .mapToObj(i -> new Genre(null, "Genre_" + i)).toList()).stream().toList();
        var books = mongoTemplate.insertAll(IntStream.range(0, BOOKS)
                .mapToObj(i -> new Book(null, "Book_" + i, authors.get(i % AUTHORS),
                        List.of(genres.get(i % GENRES), genres.get((i + 1) % GENRES))))
                .toList()).stream().toList();
        mongoTemplate.insertAll(books.stream()
                .flatMap(book -> IntStream.range(0, COMMENTS_PER_BOOK)
                        .mapToObj(j -> new BookComment("Comment_%s_%d".formatted(book.getTitle(), j), book)))
                .toList());
    }

    public void seedSql() {
        clearSql();
        for (int i = 0; i < AUTHORS; i++) {
            jdbcTemplate.update("insert into authors (full_name) values (?)", "Author_" + i);
        }
        for (int i = 0; i < GENRES; i++) {
            jdbcTemplate.update("insert into genres (name) values (?)", "Genre_" + i);
        }
        var authorIds = jdbcTemplate.queryForList("select id from authors order by id", Long.class);
        var genreIds = jdbcTemplate.queryForList("select id from genres order by id", Long.class);
        for (int i = 0; i < BOOKS; i++) {
            jdbcTemplate.update("insert into books (title, author_id) values (?, ?)",
                    "Book_" + i, authorIds.get(i % AUTHORS));
            var bookId = jdbcTemplate.queryForObject("select id from books where title = ?", Long.class,
                    "Book_" + i);
            jdbcTemplate.update("insert into books_genres (book_id, genre_id) values (?, ?), (?, ?)",
                    bookId, genreIds.get(i % GENRES), bookId, genreIds.get((i + 1) % GENRES));
            for (int j = 0; j < COMMENTS_PER_BOOK; j++) {
                jdbcTemplate.update("insert into book_comments (text, book_id) values (?, ?)",
                        "Comment_Book_%d_%d".formatted(i, j), bookId);
            }
        }
    }

    public Set<String> sqlBooks() {
        var genres = jdbcTemplate.query("""
                        select b.title, g.name from books b
                        join books_genres bg on bg.book_id = b.id join genres g on g.id = bg.genre_id""",
                (rs, n) -> List.of(rs.getString(1), rs.getString(2)));
        var comments = jdbcTemplate.query(
                "select b.title, c.text from books b join book_comments c on c.book_id = b.id",
                (rs, n) -> List.of(rs.getString(1), rs.getString(2)));
        return new TreeSet<>(jdbcTemplate.query(
                "select b.title, a.full_name from books b left join authors a on a.id = b.author_id",
                (rs, n) -> signature(rs.getString(1), rs.getString(2),
                        valuesOf(genres, rs.getString(1)), valuesOf(comments, rs.getString(1)))));
    }

    public Set<String> mongoBooks() {
        var titlesById = mongoTemplate.findAll(Book.class).stream()
                .collect(Collectors.toMap(Book::getId, Book::getTitle));
        var comments = mongoTemplate.findAll(Document.class, "book_comments").stream()
                .map(c -> List.of(titlesById.get(((DBRef) c.get("book")).getId().toString()), c.getString("text")))
                .toList();
        return mongoTemplate.findAll(Book.class).stream()
                .map(book -> signature(book.getTitle(), book.getAuthor().getFullName(),
                        book.getGenres().stream().map(Genre::getName).collect(Collectors.toCollection(TreeSet::new)),
                        valuesOf(comments, book.getTitle())))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public Map<String, Long> sqlCounts() {
        return Map.of("authors", count("authors"), "genres", count("genres"), "books", count("books"),
                "books_genres", count("books_genres"), "book_comments", count("book_comments"));
    }

    public List<String> temporaryTables() {
        return jdbcTemplate.queryForList(
                "select lower(table_name) from information_schema.tables where lower(table_name) like 'tmp_%'",
                String.class);
    }

    public List<String> temporaryCollections() {
        return new ArrayList<>(mongoTemplate.getCollectionNames().stream()
                .filter(name -> name.startsWith("tmp_")).toList());
    }

    private long count(String table) {
        var count = jdbcTemplate.queryForObject("select count(*) from " + table, Long.class);
        return count == null ? 0 : count;
    }

    private static Set<String> valuesOf(List<List<String>> pairs, String title) {
        return pairs.stream().filter(pair -> pair.get(0).equals(title)).map(pair -> pair.get(1))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private static String signature(String title, String author, Set<String> genres, Set<String> comments) {
        return "%s | %s | %s | %s".formatted(title, author, genres, comments);
    }
}
