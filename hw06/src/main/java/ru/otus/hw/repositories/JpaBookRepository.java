package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaBookRepository implements BookRepository {

    private final EntityManager entityManager;

    @Override
    public Optional<Book> findById(long id) {
        var book = entityManager.find(Book.class, id,
                Map.of("jakarta.persistence.fetchgraph", entityManager.getEntityGraph("Book.withAuthorAndGenres")));
        return Optional.ofNullable(book);
    }

    @Override
    public List<Book> findAll() {
        var books = entityManager.createQuery("select b from Book b order by b.id", Book.class)
                .setHint("jakarta.persistence.fetchgraph", entityManager.getEntityGraph("Book.withAuthor"))
                .getResultList();

        entityManager.createQuery("select b from Book b order by b.id", Book.class)
                .setHint("jakarta.persistence.fetchgraph", entityManager.getEntityGraph("Book.withGenres"))
                .getResultList();

        return books;
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            entityManager.persist(book);
            return book;
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
