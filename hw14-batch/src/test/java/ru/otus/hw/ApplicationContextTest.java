package ru.otus.hw;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Контекст приложения")
@SpringBootTest
class ApplicationContextTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @DisplayName("должен создавать пустые таблицы в реляционной БД")
    @Test
    void shouldCreateEmptyRelationalTables() {
        assertThat(count("authors")).isZero();
        assertThat(count("genres")).isZero();
        assertThat(count("books")).isZero();
        assertThat(count("books_genres")).isZero();
        assertThat(count("book_comments")).isZero();
    }

    @DisplayName("должен создавать таблицы метаданных Spring Batch")
    @Test
    void shouldCreateBatchMetadataTables() {
        assertThat(count("batch_job_instance")).isZero();
    }

    @DisplayName("должен создавать демо-данные в MongoDB")
    @Test
    void shouldInitializeMongoDatabase() {
        assertThat(mongoTemplate.findAll(Author.class)).hasSize(3);
        assertThat(mongoTemplate.findAll(Genre.class)).hasSize(6);
        assertThat(mongoTemplate.findAll(Book.class)).hasSize(3);
    }

    private Integer count(String table) {
        return jdbcTemplate.queryForObject("select count(*) from " + table, Integer.class);
    }
}
