package ru.otus.hw.listeners;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeDeleteEvent;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.BookCommentRepository;

@RequiredArgsConstructor
@Component
public class BookCommentCascadeDeleteListener extends AbstractMongoEventListener<Book> {

    private final BookCommentRepository bookCommentRepository;

    @Override
    public void onBeforeDelete(BeforeDeleteEvent<Book> event) {
        var bookId = event.getDocument().get("_id");
        if (bookId != null) {
            bookCommentRepository.deleteAllByBookId(bookId.toString());
        }
    }
}
