package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaBookRepository implements BookRepository {

    private final EntityManager entityManager;

    @Override
    public Optional<Book> findById(long id) {
        var books = entityManager.createQuery(
                        "select b from Book b join fetch b.author where b.id = :id", Book.class)
                .setParameter("id", id)
                .getResultList();
        if (books.isEmpty()) {
            return Optional.empty();
        }

        entityManager.createQuery(
                        "select b from Book b left join fetch b.genres where b.id = :id", Book.class)
                .setParameter("id", id)
                .getResultList();

        return Optional.of(books.get(0));
    }

    @Override
    public List<Book> findAll() {
        var books = entityManager.createQuery(
                        "select b from Book b join fetch b.author order by b.id", Book.class)
                .getResultList();

        entityManager.createQuery(
                        "select b from Book b left join fetch b.genres order by b.id", Book.class)
                .getResultList();

        return books;
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            entityManager.persist(book);
            return book;
        }

        if (entityManager.find(Book.class, book.getId()) == null) {
            throw new EntityNotFoundException("Book with id %d not found".formatted(book.getId()));
        }
        return entityManager.merge(book);
    }

    @Override
    public void deleteById(long id) {
        var book = entityManager.find(Book.class, id);
        if (book != null) {
            entityManager.remove(book);
        }
    }
}
