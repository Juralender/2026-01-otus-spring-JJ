package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional(readOnly = true)
    @Override
    public Optional<BookComment> findById(long id) {
        return bookCommentRepository.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookComment> findAllByBookId(long bookId) {
        return bookCommentRepository.findAllByBookId(bookId);
    }

    @Transactional
    @Override
    public BookComment insert(String text, long bookId) {
        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %d not found".formatted(bookId)));
        return bookCommentRepository.save(new BookComment(text, book));
    }

    @Transactional
    @Override
    public BookComment update(long id, String text) {
        var comment = bookCommentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment with id %d not found".formatted(id)));
        comment.setText(text);
        return comment;
    }

    @Transactional
    @Override
    public void deleteById(long id) {
        bookCommentRepository.deleteById(id);
    }
}
