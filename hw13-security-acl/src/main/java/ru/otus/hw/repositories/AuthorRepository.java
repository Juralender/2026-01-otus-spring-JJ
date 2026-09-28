package ru.otus.hw.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.models.Author;

import java.util.List;

public interface AuthorRepository extends JpaRepository<Author, Long> {

    @Override
    default List<Author> findAll() {
        return findAll(Sort.by("id"));
    }
}
