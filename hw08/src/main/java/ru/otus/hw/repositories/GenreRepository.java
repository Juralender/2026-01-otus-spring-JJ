package ru.otus.hw.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;

public interface GenreRepository extends MongoRepository<Genre, String> {

    @Override
    default List<Genre> findAll() {
        return findAll(Sort.by("id"));
    }

    List<Genre> findAllByIdIn(Set<String> ids);
}
