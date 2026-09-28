package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.StringJoiner;

@RequiredArgsConstructor
@Service
public class SqlDataFileServiceImpl implements SqlDataFileService {

    private static final List<TableSpec> TABLES = List.of(
            new TableSpec("authors", List.of("id", "full_name"), true),
            new TableSpec("genres", List.of("id", "name"), true),
            new TableSpec("books", List.of("id", "title", "author_id"), true),
            new TableSpec("books_genres", List.of("book_id", "genre_id"), false),
            new TableSpec("book_comments", List.of("id", "text", "book_id"), true));

    private final JdbcTemplate jdbcTemplate;

    private final DataSource dataSource;

    @Transactional(readOnly = true)
    @Override
    public void save(Path file) {
        try (var writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (TableSpec table : TABLES) {
                writeTable(writer, table);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to save relational data to " + file, e);
        }
    }

    @Transactional
    @Override
    public void restore(Path file) {
        if (!Files.isReadable(file)) {
            throw new IllegalArgumentException("File %s not found or not readable".formatted(file));
        }
        for (int i = TABLES.size() - 1; i >= 0; i--) {
            jdbcTemplate.update("delete from " + TABLES.get(i).name());
        }
        var populator = new ResourceDatabasePopulator(new FileSystemResource(file));
        populator.setSqlScriptEncoding(StandardCharsets.UTF_8.name());
        DatabasePopulatorUtils.execute(populator, dataSource);
        TABLES.stream().filter(TableSpec::identity).forEach(this::restartIdentity);
    }

    private void writeTable(Writer writer, TableSpec table) throws IOException {
        var columns = String.join(", ", table.columns());
        var rows = jdbcTemplate.query("select %s from %s order by %s".formatted(columns, table.name(), columns),
                (rs, n) -> {
                    var values = new StringJoiner(", ");
                    for (String column : table.columns()) {
                        values.add(sqlLiteral(rs.getObject(column)));
                    }
                    return values.toString();
                });
        for (String values : rows) {
            writer.write("insert into %s (%s) values (%s);%n".formatted(table.name(), columns, values));
        }
    }

    private void restartIdentity(TableSpec table) {
        var next = jdbcTemplate.queryForObject("select coalesce(max(id), 0) + 1 from " + table.name(), Long.class);
        jdbcTemplate.execute("alter table %s alter column id restart with %d".formatted(table.name(), next));
    }

    private static String sqlLiteral(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number) {
            return value.toString();
        }
        return "'" + value.toString().replace("'", "''") + "'";
    }

    private record TableSpec(String name, List<String> columns, boolean identity) {
    }
}
