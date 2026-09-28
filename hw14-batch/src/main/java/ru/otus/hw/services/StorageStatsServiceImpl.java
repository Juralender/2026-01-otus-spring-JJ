package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.models.Genre;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class StorageStatsServiceImpl implements StorageStatsService {

    private static final List<String> TABLES = List.of("authors", "genres", "books", "books_genres", "book_comments");

    private static final List<Class<?>> DOCUMENT_TYPES =
            List.of(Author.class, Genre.class, Book.class, BookComment.class);

    private final JdbcTemplate jdbcTemplate;

    private final MongoTemplate mongoTemplate;

    @Override
    public Map<String, Long> relationalCounts() {
        var counts = new LinkedHashMap<String, Long>();
        TABLES.forEach(table -> counts.put(table,
                jdbcTemplate.queryForObject("select count(*) from " + table, Long.class)));
        return counts;
    }

    @Override
    public Map<String, Long> mongoCounts() {
        var counts = new LinkedHashMap<String, Long>();
        DOCUMENT_TYPES.forEach(type -> counts.put(mongoTemplate.getCollectionName(type),
                mongoTemplate.count(new Query(), type)));
        return counts;
    }
}
