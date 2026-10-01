package ru.otus.hw.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;

@RepositoryRestResource(path = "genres", collectionResourceRel = "genres")
public interface GenreRepository extends JpaRepository<Genre, Long> {

    @Override
    default List<Genre> findAll() {
        return findAll(Sort.by("id"));
    }

    List<Genre> findAllByIdIn(Set<Long> ids);
}
