package ru.otus.hw.batch.importing;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.otus.hw.batch.MigrationMode;
import ru.otus.hw.batch.config.MigrationProperties;
import ru.otus.hw.batch.config.MigrationStepFactory;
import ru.otus.hw.batch.config.MigrationSteps;
import ru.otus.hw.batch.mapping.EntityKind;
import ru.otus.hw.batch.mapping.IdMappingStore;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.models.Genre;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class ImportJobConfig {

    public static final String IMPORT_RAM_JOB = "importRamJob";

    public static final String IMPORT_DB_JOB = "importDbJob";

    private static final List<Class<?>> TARGET_TYPES =
            List.of(BookComment.class, Book.class, Author.class, Genre.class);

    private final MigrationStepFactory stepFactory;

    private final MigrationProperties properties;

    private final MongoTemplate mongoTemplate;

    private final NamedParameterJdbcTemplate jdbc;

    private final DataSource dataSource;

    private final MongoImportSupport support;

    @Bean
    public Job importRamJob(@Qualifier("ramImportIdMappingStore") IdMappingStore<Long, String> idStore) {
        return stepFactory.job(IMPORT_RAM_JOB, MigrationMode.RAM, steps(IMPORT_RAM_JOB, MigrationMode.RAM, idStore));
    }

    @Bean
    public Job importDbJob(@Qualifier("dbImportIdMappingStore") IdMappingStore<Long, String> idStore) {
        return stepFactory.job(IMPORT_DB_JOB, MigrationMode.DB, steps(IMPORT_DB_JOB, MigrationMode.DB, idStore));
    }

    private MigrationSteps steps(String job, MigrationMode mode, IdMappingStore<Long, String> idStore) {
        return new MigrationSteps(
                stepFactory.taskletStep(job + "Prepare", () -> {
                    TARGET_TYPES.forEach(type -> mongoTemplate.remove(new Query(), type));
                    idStore.prepare();
                }),
                authorsStep(job, mode, idStore),
                genresStep(job, mode, idStore),
                stepFactory.chunkStep(job + "Books", mode,
                        reader(job + "BooksReader", mode, "select id, title, author_id, author_full_name",
                                "from (select b.id, b.title, b.author_id, a.full_name author_full_name "
                                        + "from books b left join authors a on a.id = b.author_id) t",
                                (rs, n) -> new BookRow(rs.getLong("id"), rs.getString("title"),
                                        rs.getObject("author_id", Long.class), rs.getString("author_full_name"))),
                        new BookImportWriter(support, idStore, jdbc)),
                stepFactory.chunkStep(job + "Comments", mode,
                        reader(job + "CommentsReader", mode, "select id, text, book_id", "from book_comments",
                                (rs, n) -> new BookCommentRow(rs.getLong("id"), rs.getString("text"),
                                        rs.getObject("book_id", Long.class))),
                        new BookCommentImportWriter(support, idStore)),
                stepFactory.taskletStep(job + "Cleanup", idStore::cleanup));
    }

    private Step authorsStep(String job, MigrationMode mode, IdMappingStore<Long, String> idStore) {
        return stepFactory.chunkStep(job + "Authors", mode,
                reader(job + "AuthorsReader", mode, "select id, full_name", "from authors",
                        (rs, n) -> new AuthorRow(rs.getLong("id"), rs.getString("full_name"))),
                new SimpleImportWriter<>(support, idStore, EntityKind.AUTHOR, Author.class, AuthorRow::id,
                        (row, id) -> new Author(id, row.fullName()), Author::getId));
    }

    private Step genresStep(String job, MigrationMode mode, IdMappingStore<Long, String> idStore) {
        return stepFactory.chunkStep(job + "Genres", mode,
                reader(job + "GenresReader", mode, "select id, name", "from genres",
                        (rs, n) -> new GenreRow(rs.getLong("id"), rs.getString("name"))),
                new SimpleImportWriter<>(support, idStore, EntityKind.GENRE, Genre.class, GenreRow::id,
                        (row, id) -> new Genre(id, row.name()), Genre::getId));
    }

    private <T> ItemStreamReader<T> reader(String name, MigrationMode mode, String select, String from,
                                           RowMapper<T> rowMapper) {
        var reader = new JdbcPagingItemReaderBuilder<T>()
                .name(name)
                .dataSource(dataSource)
                .selectClause(select)
                .fromClause(from)
                .sortKeys(Map.of("id", Order.ASCENDING))
                .rowMapper(rowMapper)
                .pageSize(properties.chunkSize())
                .saveState(mode == MigrationMode.DB)
                .build();
        try {
            reader.afterPropertiesSet();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize reader " + name, e);
        }
        return reader;
    }
}
