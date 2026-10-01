package ru.otus.hw.batch.exporting;

import com.mongodb.DBRef;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import ru.otus.hw.batch.mapping.EntityKind;
import ru.otus.hw.batch.mapping.IdMappingStore;

import java.util.Objects;

import static ru.otus.hw.batch.mapping.IdMappings.require;

@RequiredArgsConstructor
public class BookCommentExportWriter implements ItemWriter<Document> {

    private final JdbcBatchInserter inserter;

    private final IdMappingStore<String, Long> idStore;

    @Override
    public void write(Chunk<? extends Document> chunk) {
        var comments = chunk.getItems();
        var bookIds = idStore.getAll(EntityKind.BOOK, comments.stream()
                .map(BookCommentExportWriter::bookId).filter(Objects::nonNull).distinct().toList());
        idStore.putAll(EntityKind.COMMENT, inserter.<Document>insert(
                "insert into book_comments (text, book_id) values (:text, :bookId)",
                comments, BookCommentExportWriter::id, comment -> new MapSqlParameterSource()
                        .addValue("text", comment.getString("text"))
                        .addValue("bookId", require(bookIds, bookId(comment), EntityKind.BOOK))));
    }

    private static String id(Document comment) {
        return comment.get("_id").toString();
    }

    private static String bookId(Document comment) {
        return comment.get("book") instanceof DBRef book ? book.getId().toString() : null;
    }
}
