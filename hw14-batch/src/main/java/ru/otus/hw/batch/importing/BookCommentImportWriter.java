package ru.otus.hw.batch.importing;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import ru.otus.hw.batch.mapping.EntityKind;
import ru.otus.hw.batch.mapping.IdMappingStore;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;

import java.util.Map;
import java.util.Objects;

import static ru.otus.hw.batch.mapping.IdMappings.require;

@RequiredArgsConstructor
public class BookCommentImportWriter implements ItemWriter<BookCommentRow> {

    private final MongoImportSupport support;

    private final IdMappingStore<Long, String> idStore;

    @Override
    public void write(Chunk<? extends BookCommentRow> chunk) {
        var rows = chunk.getItems();
        var commentIds = support.assignIds(idStore, EntityKind.COMMENT,
                rows.stream().map(BookCommentRow::id).toList());
        var bookIds = idStore.getAll(EntityKind.BOOK,
                rows.stream().map(BookCommentRow::bookId).filter(Objects::nonNull).distinct().toList());
        var comments = rows.stream()
                .map(row -> new BookComment(commentIds.get(row.id()), row.text(), book(row, bookIds)))
                .toList();
        support.upsertAll(BookComment.class, comments, BookComment::getId);
    }

    private Book book(BookCommentRow row, Map<Long, String> bookIds) {
        if (row.bookId() == null) {
            return null;
        }
        return new Book(require(bookIds, row.bookId(), EntityKind.BOOK), null, null, null);
    }
}
