package ru.otus.hw.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    @Override
    default List<Genre> findAll() {
        return findAll(Sort.by("id"));
    }

    List<Genre> findAllByIdIn(Set<Long> ids);
}
