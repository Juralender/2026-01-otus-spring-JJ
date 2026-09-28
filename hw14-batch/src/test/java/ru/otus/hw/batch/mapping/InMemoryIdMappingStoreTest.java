package ru.otus.hw.batch.mapping;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Хранилище соответствия идентификаторов в памяти")
class InMemoryIdMappingStoreTest {

    private InMemoryIdMappingStore<Long, String> store;

    @BeforeEach
    void setUp() {
        store = new InMemoryIdMappingStore<>();
        store.prepare();
    }

    @DisplayName("должен возвращать только сохранённые соответствия нужного типа")
    @Test
    void shouldReturnOnlyStoredMappingsOfKind() {
        store.putAll(EntityKind.AUTHOR, Map.of(1L, "a1", 2L, "a2"));
        store.putAll(EntityKind.GENRE, Map.of(1L, "g1"));

        assertThat(store.getAll(EntityKind.AUTHOR, List.of(1L, 2L, 3L))).containsExactlyInAnyOrderEntriesOf(
                Map.of(1L, "a1", 2L, "a2"));
        assertThat(store.getAll(EntityKind.GENRE, List.of(1L, 2L))).containsExactlyEntriesOf(Map.of(1L, "g1"));
        assertThat(store.getAll(EntityKind.BOOK, List.of(1L))).isEmpty();
    }

    @DisplayName("должен сохранять соответствия при параллельной записи")
    @Test
    void shouldKeepAllMappingsOnParallelWrites() throws Exception {
        int threads = 8;
        int perThread = 1_000;
        var executor = Executors.newFixedThreadPool(threads);
        try {
            List<Future<?>> futures = IntStream.range(0, threads)
                    .mapToObj(t -> executor.submit(() -> store.putAll(EntityKind.BOOK,
                            LongStream.range((long) t * perThread, (long) (t + 1) * perThread).boxed()
                                    .collect(Collectors.toMap(id -> id, id -> "b" + id)))))
                    .collect(Collectors.toList());
            for (Future<?> future : futures) {
                future.get();
            }
        } finally {
            executor.shutdown();
        }

        var allIds = LongStream.range(0, (long) threads * perThread).boxed().toList();
        var mappings = store.getAll(EntityKind.BOOK, allIds);
        assertThat(mappings).hasSize(threads * perThread);
        assertThat(mappings.get(4242L)).isEqualTo("b4242");
    }

    @DisplayName("должен очищать соответствия")
    @Test
    void shouldClearMappingsOnCleanup() {
        store.putAll(EntityKind.AUTHOR, Map.of(1L, "a1"));

        store.cleanup();

        assertThat(store.getAll(EntityKind.AUTHOR, List.of(1L))).isEmpty();
    }
}
