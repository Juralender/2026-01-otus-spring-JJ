package ru.otus.hw.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(path = "books", collectionResourceRel = "books")
public interface BookRepository extends JpaRepository<Book, Long> {

    @Override
    @EntityGraph(value = "Book.withAuthorAndGenres")
    Optional<Book> findById(Long id);

    @Override
    @EntityGraph(value = "Book.withAuthor")
    List<Book> findAll(Sort sort);

    @Override
    default List<Book> findAll() {
        return findAll(Sort.by("id"));
    }
}
