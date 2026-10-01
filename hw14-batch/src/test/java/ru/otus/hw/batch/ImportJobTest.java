package ru.otus.hw.batch;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.batch.core.BatchStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.MigrationService;
import ru.otus.hw.services.SqlDataFileService;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.otus.hw.batch.MigrationTestData.AUTHORS;
import static ru.otus.hw.batch.MigrationTestData.BOOKS;
import static ru.otus.hw.batch.MigrationTestData.COMMENTS_PER_BOOK;
import static ru.otus.hw.batch.MigrationTestData.GENRES;

@DisplayName("Импорт данных из реляционной БД в MongoDB")
@SpringBootTest
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:migration;DB_CLOSE_DELAY=-1")
class ImportJobTest {

    @Autowired
    private MigrationService migrationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private SqlDataFileService sqlDataFileService;

    @TempDir
    private Path tempDir;

    private MigrationTestData data;

    @BeforeEach
    void setUp() {
        data = new MigrationTestData(jdbcTemplate, mongoTemplate);
        data.seedSql();
        data.clearMongo();
    }

    @DisplayName("должен переносить все сущности с сохранением связей и удалять временные данные")
    @ParameterizedTest
    @EnumSource(MigrationMode.class)
    void shouldImportAllEntitiesKeepingRelations(MigrationMode mode) {
        var execution = migrationService.importToMongo(mode);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(mongoTemplate.findAll(Author.class)).hasSize(AUTHORS);
        assertThat(mongoTemplate.findAll(Genre.class)).hasSize(GENRES);
        assertThat(mongoTemplate.findAll(Book.class)).hasSize(BOOKS);
        assertThat(mongoTemplate.findAll(Document.class, "book_comments")).hasSize(COMMENTS_PER_BOOK * BOOKS);
        assertThat(data.mongoBooks()).isEqualTo(data.sqlBooks());
        assertThat(data.temporaryCollections()).isEmpty();
    }

    @DisplayName("должен ссылаться во встроенных документах на существующих авторов и жанры")
    @ParameterizedTest
    @EnumSource(MigrationMode.class)
    void shouldReferenceExistingAuthorsAndGenres(MigrationMode mode) {
        migrationService.importToMongo(mode);

        var authors = mongoTemplate.findAll(Author.class).stream()
                .collect(Collectors.toMap(Author::getId, Author::getFullName));
        var genres = mongoTemplate.findAll(Genre.class).stream()
                .collect(Collectors.toMap(Genre::getId, Genre::getName));
        assertThat(mongoTemplate.findAll(Book.class)).allSatisfy(book -> {
            assertThat(authors).containsEntry(book.getAuthor().getId(), book.getAuthor().getFullName());
            assertThat(book.getGenres()).allSatisfy(genre ->
                    assertThat(genres).containsEntry(genre.getId(), genre.getName()));
        });
    }

    @DisplayName("должен сохранять в MongoDB только идентификаторы ObjectId")
    @ParameterizedTest
    @EnumSource(MigrationMode.class)
    void shouldUseObjectIdsOnly(MigrationMode mode) {
        migrationService.importToMongo(mode);

        for (String collection : List.of("authors", "genres", "books", "book_comments")) {
            assertThat(mongoTemplate.findAll(Document.class, collection))
                    .allSatisfy(document -> assertThat(document.get("_id")).isInstanceOf(ObjectId.class));
        }
    }

    @DisplayName("должен очищать MongoDB перед повторным импортом")
    @ParameterizedTest
    @EnumSource(MigrationMode.class)
    void shouldReplaceTargetDataOnRepeatedImport(MigrationMode mode) {
        migrationService.importToMongo(mode);

        var execution = migrationService.importToMongo(mode);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(mongoTemplate.findAll(Book.class)).hasSize(BOOKS);
        assertThat(mongoTemplate.findAll(Document.class, "book_comments")).hasSize(COMMENTS_PER_BOOK * BOOKS);
    }

    @DisplayName("должен продолжать упавший импорт в режиме DB без дублирования данных")
    @Test
    void shouldRestartFailedDbImport() {
        jdbcTemplate.execute("set referential_integrity false");
        jdbcTemplate.update("insert into book_comments (text, book_id) values ('Broken comment', 999999)");
        jdbcTemplate.execute("set referential_integrity true");

        var failed = migrationService.importToMongo(MigrationMode.DB);

        assertThat(failed.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(data.temporaryCollections()).isNotEmpty();

        jdbcTemplate.update("delete from book_comments where book_id = 999999");
        var restarted = migrationService.restart(failed.getId());

        assertThat(restarted.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(mongoTemplate.findAll(Book.class)).hasSize(BOOKS);
        assertThat(mongoTemplate.findAll(Document.class, "book_comments")).hasSize(COMMENTS_PER_BOOK * BOOKS);
        assertThat(data.mongoBooks()).isEqualTo(data.sqlBooks());
        assertThat(data.temporaryCollections()).isEmpty();
    }

    @DisplayName("должен восстанавливать исходные данные MongoDB после экспорта и импорта")
    @ParameterizedTest
    @EnumSource(MigrationMode.class)
    void shouldKeepDataAfterRoundTrip(MigrationMode mode) {
        data.seedMongo();
        var original = data.mongoBooks();

        migrationService.exportToSql(mode);
        data.clearMongo();
        migrationService.importToMongo(mode);

        assertThat(data.mongoBooks()).isEqualTo(original);
    }

    @DisplayName("должен восстанавливать данные MongoDB из файла, сохранённого после экспорта")
    @ParameterizedTest
    @EnumSource(MigrationMode.class)
    void shouldKeepDataAfterRoundTripThroughFile(MigrationMode mode) {
        data.seedMongo();
        var original = data.mongoBooks();
        var file = tempDir.resolve("library.sql");

        migrationService.exportToSql(mode);
        sqlDataFileService.save(file);
        data.clearSql();
        data.clearMongo();
        sqlDataFileService.restore(file);
        migrationService.importToMongo(mode);

        assertThat(data.mongoBooks()).isEqualTo(original);
    }
}
