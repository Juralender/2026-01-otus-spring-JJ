package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.BookComment;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaBookCommentRepository implements BookCommentRepository {

    private final EntityManager entityManager;

    @Override
    public Optional<BookComment> findById(long id) {
        var comment = entityManager.find(BookComment.class, id,
                Map.of("jakarta.persistence.fetchgraph", entityManager.getEntityGraph("BookComment.withBook")));
        return Optional.ofNullable(comment);
    }

    @Override
    public List<BookComment> findAllByBookId(long bookId) {
        return entityManager.createQuery(
                        "select c from BookComment c where c.book.id = :bookId order by c.id",
                        BookComment.class)
                .setParameter("bookId", bookId)
                .setHint("jakarta.persistence.fetchgraph", entityManager.getEntityGraph("BookComment.withBook"))
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
