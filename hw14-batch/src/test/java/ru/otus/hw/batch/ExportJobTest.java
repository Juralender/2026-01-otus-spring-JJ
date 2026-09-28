package ru.otus.hw.batch;

import com.mongodb.DBRef;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.batch.core.BatchStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import ru.otus.hw.services.MigrationService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static ru.otus.hw.batch.MigrationTestData.AUTHORS;
import static ru.otus.hw.batch.MigrationTestData.BOOKS;
import static ru.otus.hw.batch.MigrationTestData.COMMENTS_PER_BOOK;
import static ru.otus.hw.batch.MigrationTestData.GENRES;

@DisplayName("Экспорт данных из MongoDB в реляционную БД")
@SpringBootTest
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:migration;DB_CLOSE_DELAY=-1")
class ExportJobTest {

    @Autowired
    private MigrationService migrationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    private MigrationTestData data;

    @BeforeEach
    void setUp() {
        data = new MigrationTestData(jdbcTemplate, mongoTemplate);
        data.seedMongo();
        data.clearSql();
    }

    @DisplayName("должен переносить все сущности с сохранением связей и удалять временные данные")
    @ParameterizedTest
    @EnumSource(MigrationMode.class)
    void shouldExportAllEntitiesKeepingRelations(MigrationMode mode) {
        var execution = migrationService.exportToSql(mode);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(data.sqlCounts()).containsExactlyInAnyOrderEntriesOf(expectedCounts());
        assertThat(data.sqlBooks()).isEqualTo(data.mongoBooks());
        assertThat(data.temporaryTables()).isEmpty();
    }

    @DisplayName("должен очищать целевую БД перед повторным экспортом")
    @ParameterizedTest
    @EnumSource(MigrationMode.class)
    void shouldReplaceTargetDataOnRepeatedExport(MigrationMode mode) {
        migrationService.exportToSql(mode);

        var execution = migrationService.exportToSql(mode);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(data.sqlCounts()).containsExactlyInAnyOrderEntriesOf(expectedCounts());
    }

    @DisplayName("должен продолжать упавший экспорт в режиме DB после исправления данных")
    @Test
    void shouldRestartFailedDbExport() {
        var brokenCommentId = insertCommentOfMissingBook();

        var failed = migrationService.exportToSql(MigrationMode.DB);

        assertThat(failed.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(data.temporaryTables()).isNotEmpty();

        mongoTemplate.remove(query(where("_id").is(brokenCommentId)), "book_comments");
        var restarted = migrationService.restart(failed.getId());

        assertThat(restarted.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(restarted.getJobInstance().getInstanceId()).isEqualTo(failed.getJobInstance().getInstanceId());
        assertThat(data.sqlCounts()).containsExactlyInAnyOrderEntriesOf(expectedCounts());
        assertThat(data.sqlBooks()).isEqualTo(data.mongoBooks());
        assertThat(data.temporaryTables()).isEmpty();
    }

    @DisplayName("не должен перезапускать экспорт в режиме RAM")
    @Test
    void shouldNotRestartRamExport() {
        insertCommentOfMissingBook();
        var failed = migrationService.exportToSql(MigrationMode.RAM);

        assertThat(failed.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThatThrownBy(() -> migrationService.restart(failed.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RAM");
    }

    @DisplayName("не должен перезапускать успешно завершённый экспорт")
    @Test
    void shouldNotRestartCompletedExport() {
        var completed = migrationService.exportToSql(MigrationMode.DB);

        assertThatThrownBy(() -> migrationService.restart(completed.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("COMPLETED");
    }

    private ObjectId insertCommentOfMissingBook() {
        var id = new ObjectId();
        mongoTemplate.getCollection("book_comments").insertOne(new Document("_id", id)
                .append("text", "Broken comment")
                .append("book", new DBRef("books", new ObjectId())));
        return id;
    }

    private static Map<String, Long> expectedCounts() {
        return Map.of("authors", (long) AUTHORS, "genres", (long) GENRES, "books", (long) BOOKS,
                "books_genres", 2L * BOOKS, "book_comments", (long) COMMENTS_PER_BOOK * BOOKS);
    }
}
