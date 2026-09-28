package ru.otus.hw.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.models.Author;

import java.util.List;

public interface AuthorRepository extends MongoRepository<Author, String> {

    @Override
    default List<Author> findAll() {
        return findAll(Sort.by("id"));
    }
}
