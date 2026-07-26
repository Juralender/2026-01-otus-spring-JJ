package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.BookComment;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaBookCommentRepository implements BookCommentRepository {

    private final EntityManager entityManager;

    @Override
    public Optional<BookComment> findById(long id) {
        return entityManager.createQuery(
                        "select c from BookComment c join fetch c.book where c.id = :id", BookComment.class)
                .setParameter("id", id)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public List<BookComment> findAllByBookId(long bookId) {
        return entityManager.createQuery(
                        "select c from BookComment c join fetch c.book where c.book.id = :bookId order by c.id",
                        BookComment.class)
                .setParameter("bookId", bookId)
                .getResultList();
    }

    @Override
    public BookComment save(BookComment comment) {
        if (comment.getId() == 0) {
            entityManager.persist(comment);
            return comment;
        }

        if (entityManager.find(BookComment.class, comment.getId()) == null) {
            throw new EntityNotFoundException("Comment with id %d not found".formatted(comment.getId()));
        }
        return entityManager.merge(comment);
    }

    @Override
    public void deleteById(long id) {
        var comment = entityManager.find(BookComment.class, id);
        if (comment != null) {
            entityManager.remove(comment);
        }
    }
}
