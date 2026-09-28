package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Сервис сохранения и восстановления данных реляционной БД в файле")
@JdbcTest
@Import(SqlDataFileServiceImpl.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class SqlDataFileServiceTest {

    private static final List<String> TABLES =
            List.of("authors", "genres", "books", "books_genres", "book_comments");

    @Autowired
    private SqlDataFileService sqlDataFileService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() {
        clearTables();
        jdbcTemplate.update("insert into authors (full_name) values ('Author_1'), ('Author_2')");
        jdbcTemplate.update("insert into genres (name) values ('Genre_1'), ('Genre_2')");
        jdbcTemplate.update("insert into books (title, author_id) values "
                + "('Book_1', (select id from authors where full_name = 'Author_1')), "
                + "('Book_2', (select id from authors where full_name = 'Author_2'))");
        jdbcTemplate.update("""
                insert into books_genres (book_id, genre_id)
                select b.id, g.id from books b, genres g
                where b.title = 'Book_1' or g.name = 'Genre_2'""");
        jdbcTemplate.update("insert into book_comments (text, book_id) values "
                + "('Comment_1', (select id from books where title = 'Book_1'))");
    }

    @DisplayName("должен восстанавливать из файла все данные с сохранением связей")
    @Test
    void shouldRestoreSavedData() {
        var file = tempDir.resolve("library.sql");
        var before = snapshot();

        sqlDataFileService.save(file);
        clearTables();
        sqlDataFileService.restore(file);

        assertThat(snapshot()).isEqualTo(before);
    }

    @DisplayName("должен сохранять данные в виде insert-выражений")
    @Test
    void shouldSaveInsertStatements() throws IOException {
        var file = tempDir.resolve("library.sql");

        sqlDataFileService.save(file);

        var bookId = jdbcTemplate.queryForObject("select id from books where title = 'Book_1'", Long.class);
        var genreId = jdbcTemplate.queryForObject("select id from genres where name = 'Genre_1'", Long.class);
        assertThat(Files.readAllLines(file))
                .hasSize(10)
                .allMatch(line -> line.startsWith("insert into "))
                .contains("insert into books_genres (book_id, genre_id) values (%d, %d);".formatted(bookId, genreId));
    }

    @DisplayName("должен заменять текущие данные данными из файла")
    @Test
    void shouldReplaceExistingData() {
        var file = tempDir.resolve("library.sql");
        var before = snapshot();
        sqlDataFileService.save(file);
        jdbcTemplate.update("insert into authors (full_name) values ('Extra author')");

        sqlDataFileService.restore(file);

        assertThat(snapshot()).isEqualTo(before);
    }

    @DisplayName("должен корректно сохранять строки со спецсимволами")
    @Test
    void shouldKeepSpecialCharacters() {
        var file = tempDir.resolve("library.sql");
        var name = "O'Brien; -- not a comment";
        jdbcTemplate.update("insert into authors (full_name) values (?)", name);
        var before = snapshot();

        sqlDataFileService.save(file);
        clearTables();
        sqlDataFileService.restore(file);

        assertThat(snapshot()).isEqualTo(before);
        assertThat(jdbcTemplate.queryForList("select full_name from authors", String.class)).contains(name);
    }

    @DisplayName("должен продолжать генерацию идентификаторов после восстановления")
    @Test
    void shouldContinueIdGenerationAfterRestore() {
        var file = tempDir.resolve("library.sql");
        sqlDataFileService.save(file);
        clearTables();
        sqlDataFileService.restore(file);
        var maxId = jdbcTemplate.queryForObject("select max(id) from authors", Long.class);

        jdbcTemplate.update("insert into authors (full_name) values ('New author')");

        var newId = jdbcTemplate.queryForObject(
                "select id from authors where full_name = 'New author'", Long.class);
        assertThat(newId).isGreaterThan(maxId);
    }

    @DisplayName("не должен изменять данные при ошибке в файле")
    @Test
    void shouldKeepDataWhenFileIsBroken() throws IOException {
        var file = Files.writeString(tempDir.resolve("broken.sql"),
                "insert into authors (id, full_name) values (100, 'A');\ninsert into missing_table values (1);\n");
        var before = snapshot();

        assertThatThrownBy(() -> sqlDataFileService.restore(file)).isInstanceOf(RuntimeException.class);

        assertThat(snapshot()).isEqualTo(before);
    }

    @DisplayName("должен сообщать об отсутствующем файле")
    @Test
    void shouldRejectMissingFile() {
        var before = snapshot();

        assertThatThrownBy(() -> sqlDataFileService.restore(tempDir.resolve("missing.sql")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("missing.sql");
        assertThat(snapshot()).isEqualTo(before);
    }

    private Map<String, List<Map<String, Object>>> snapshot() {
        return Map.of(
                "authors", jdbcTemplate.queryForList("select * from authors order by id"),
                "genres", jdbcTemplate.queryForList("select * from genres order by id"),
                "books", jdbcTemplate.queryForList("select * from books order by id"),
                "books_genres", jdbcTemplate.queryForList("select * from books_genres order by book_id, genre_id"),
                "book_comments", jdbcTemplate.queryForList("select * from book_comments order by id"));
    }

    private void clearTables() {
        for (int i = TABLES.size() - 1; i >= 0; i--) {
            jdbcTemplate.update("delete from " + TABLES.get(i));
        }
    }
}
