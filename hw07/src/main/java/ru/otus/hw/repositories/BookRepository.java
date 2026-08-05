package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Override
    @EntityGraph(attributePaths = {"author", "genres"})
    Optional<Book> findById(Long id);

    @EntityGraph(attributePaths = "author")
    @Query("select b from Book b order by b.id")
    List<Book> findAllWithAuthor();

    @EntityGraph(attributePaths = "genres")
    @Query("select b from Book b order by b.id")
    List<Book> findAllWithGenres();

    @Override
    default List<Book> findAll() {
        var books = findAllWithAuthor();
        findAllWithGenres();
        return books;
    }
}
