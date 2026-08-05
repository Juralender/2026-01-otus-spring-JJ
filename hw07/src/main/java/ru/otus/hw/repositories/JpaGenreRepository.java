package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class JpaGenreRepository implements GenreRepository {

    private final EntityManager entityManager;

    @Override
    public List<Genre> findAll() {
        return entityManager.createQuery("select g from Genre g order by g.id", Genre.class)
                .getResultList();
    }

    @Override
    public List<Genre> findAllByIds(Set<Long> ids) {
        return entityManager.createQuery("select g from Genre g where g.id in :ids", Genre.class)
                .setParameter("ids", ids)
                .getResultList();
    }
}
