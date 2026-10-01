package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.repositories.BookCommentRepository;
import ru.otus.hw.repositories.BookRepository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BookCommentServiceImpl implements BookCommentService {

    private final BookRepository bookRepository;

    private final BookCommentRepository bookCommentRepository;

    @Override
    public Optional<BookComment> findById(String id) {
        return bookCommentRepository.findById(id);
    }

    @Override
    public List<BookComment> findAllByBookId(String bookId) {
        return bookCommentRepository.findAllByBookIdOrderByIdAsc(bookId);
    }

    @Override
    public BookComment insert(String text, String bookId) {
        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %s not found".formatted(bookId)));
        return bookCommentRepository.save(new BookComment(text, book));
    }

    @Override
    public BookComment update(String id, String text) {
        var comment = bookCommentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment with id %s not found".formatted(id)));
        comment.setText(text);
        return bookCommentRepository.save(comment);
    }

    @Override
    public void deleteById(String id) {
        bookCommentRepository.deleteById(id);
    }
}
