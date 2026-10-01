package ru.otus.hw.batch.exporting;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.data.builder.MongoCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import ru.otus.hw.batch.MigrationMode;
import ru.otus.hw.batch.config.MigrationStepFactory;
import ru.otus.hw.batch.config.MigrationSteps;
import ru.otus.hw.batch.mapping.EntityKind;
import ru.otus.hw.batch.mapping.IdMappingStore;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class ExportJobConfig {

    public static final String EXPORT_RAM_JOB = "exportRamJob";

    public static final String EXPORT_DB_JOB = "exportDbJob";

    private static final List<String> TARGET_TABLES =
            List.of("book_comments", "books_genres", "books", "authors", "genres");

    private final MigrationStepFactory stepFactory;

    private final MongoTemplate mongoTemplate;

    private final JdbcTemplate jdbcTemplate;

    private final JdbcBatchInserter inserter;

    @Bean
    public Job exportRamJob(@Qualifier("ramExportIdMappingStore") IdMappingStore<String, Long> idStore) {
        return stepFactory.job(EXPORT_RAM_JOB, MigrationMode.RAM, steps(EXPORT_RAM_JOB, MigrationMode.RAM, idStore));
    }

    @Bean
    public Job exportDbJob(@Qualifier("dbExportIdMappingStore") IdMappingStore<String, Long> idStore) {
        return stepFactory.job(EXPORT_DB_JOB, MigrationMode.DB, steps(EXPORT_DB_JOB, MigrationMode.DB, idStore));
    }

    private MigrationSteps steps(String job, MigrationMode mode, IdMappingStore<String, Long> idStore) {
        return new MigrationSteps(
                stepFactory.taskletStep(job + "Prepare", () -> {
                    TARGET_TABLES.forEach(table -> jdbcTemplate.update("delete from " + table));
                    idStore.prepare();
                }),
                authorsStep(job, mode, idStore),
                genresStep(job, mode, idStore),
                booksStep(job, mode, idStore),
                commentsStep(job, mode, idStore),
                stepFactory.taskletStep(job + "Cleanup", idStore::cleanup));
    }

    private Step authorsStep(String job, MigrationMode mode, IdMappingStore<String, Long> idStore) {
        return stepFactory.chunkStep(job + "Authors", mode,
                reader(job + "AuthorsReader", mode, Author.class, Author.class),
                new SimpleExportWriter<Author>(inserter, idStore, EntityKind.AUTHOR,
                        "insert into authors (full_name) values (:fullName)", Author::getId,
                        author -> new MapSqlParameterSource("fullName", author.getFullName())));
    }

    private Step genresStep(String job, MigrationMode mode, IdMappingStore<String, Long> idStore) {
        return stepFactory.chunkStep(job + "Genres", mode,
                reader(job + "GenresReader", mode, Genre.class, Genre.class),
                new SimpleExportWriter<Genre>(inserter, idStore, EntityKind.GENRE,
                        "insert into genres (name) values (:name)", Genre::getId,
                        genre -> new MapSqlParameterSource("name", genre.getName())));
    }

    private Step booksStep(String job, MigrationMode mode, IdMappingStore<String, Long> idStore) {
        return stepFactory.chunkStep(job + "Books", mode,
                reader(job + "BooksReader", mode, Book.class, Book.class),
                new BookExportWriter(inserter, idStore));
    }

    private Step commentsStep(String job, MigrationMode mode, IdMappingStore<String, Long> idStore) {
        return stepFactory.chunkStep(job + "Comments", mode,
                reader(job + "CommentsReader", mode, BookComment.class, Document.class),
                new BookCommentExportWriter(inserter, idStore));
    }

    private <T> ItemStreamReader<T> reader(String name, MigrationMode mode, Class<?> entity, Class<T> type) {
        return new MongoCursorItemReaderBuilder<T>()
                .name(name)
                .template(mongoTemplate)
                .targetType(type)
                .collection(mongoTemplate.getCollectionName(entity))
                .jsonQuery("{}")
                .sorts(Map.of("_id", Sort.Direction.ASC))
                .saveState(mode == MigrationMode.DB)
                .build();
    }
}
