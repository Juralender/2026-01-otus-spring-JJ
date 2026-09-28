package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.repositories.BookCommentRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.UserRepository;
import ru.otus.hw.services.dto.BookCommentCreateDto;
import ru.otus.hw.services.dto.BookCommentDto;
import ru.otus.hw.services.dto.BookCommentUpdateDto;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BookCommentServiceImpl implements BookCommentService {

    private final BookRepository bookRepository;

    private final BookCommentRepository bookCommentRepository;

    private final UserRepository userRepository;

    private final AclServiceWrapperService aclServiceWrapperService;

    @Transactional(readOnly = true)
    @Override
    public BookCommentDto findById(long id) {
        return bookCommentRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Comment with id %d not found".formatted(id)));
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookCommentDto> findAllByBookId(long bookId) {
        return bookCommentRepository.findAllByBookIdOrderByIdAsc(bookId).stream().map(this::toDto).toList();
    }

    @Transactional
    @Override
    public BookCommentDto insert(BookCommentCreateDto bookCommentCreateDto) {
        var book = bookRepository.findById(bookCommentCreateDto.getBookId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Book with id %d not found".formatted(bookCommentCreateDto.getBookId())));
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User with username %s not found".formatted(username)));
        var comment = bookCommentRepository.save(new BookComment(bookCommentCreateDto.getText(), book, user));
        aclServiceWrapperService.grantOwnerPermissions(comment, username);
        return toDto(comment);
    }

    @PreAuthorize("hasRole('ADMIN') or hasPermission(#bookCommentUpdateDto.id, "
            + "'ru.otus.hw.models.BookComment', 'WRITE')")
    @Transactional
    @Override
    public BookCommentDto update(BookCommentUpdateDto bookCommentUpdateDto) {
        var comment = bookCommentRepository.findById(bookCommentUpdateDto.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Comment with id %d not found".formatted(bookCommentUpdateDto.getId())));
        comment.setText(bookCommentUpdateDto.getText());
        return toDto(comment);
    }

    @PreAuthorize("hasRole('ADMIN') or hasPermission(#id, 'ru.otus.hw.models.BookComment', 'DELETE')")
    @Transactional
    @Override
    public void deleteById(long id) {
        bookCommentRepository.deleteById(id);
        aclServiceWrapperService.deleteAcl(BookComment.class, id);
    }

    private BookCommentDto toDto(BookComment comment) {
        return new BookCommentDto(comment.getId(), comment.getText(), comment.getBook().getId(),
                comment.getUser().getUsername());
    }
}
