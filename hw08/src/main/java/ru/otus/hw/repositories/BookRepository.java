package ru.otus.hw.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.models.Book;

import java.util.List;

public interface BookRepository extends MongoRepository<Book, String> {

    @Override
    default List<Book> findAll() {
        return findAll(Sort.by("id"));
    }
}
