package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import ru.otus.hw.models.BookComment;

@Component
public class BookCommentConverter {

    public String bookCommentToString(BookComment comment) {
        return "Id: %d, text: %s, book: {Id: %d, title: %s}".formatted(
                comment.getId(),
                comment.getText(),
                comment.getBook().getId(),
                comment.getBook().getTitle());
    }
}
