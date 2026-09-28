package ru.otus.hw.batch.mapping;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Хранилище соответствия идентификаторов экспорта во временных таблицах")
@JdbcTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class JdbcExportIdMappingStoreTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    private JdbcExportIdMappingStore store;

    @BeforeEach
    void setUp() {
        store = new JdbcExportIdMappingStore(jdbc);
        store.prepare();
    }

    @AfterEach
    void tearDown() {
        store.cleanup();
    }

    @DisplayName("должен создавать временные таблицы для всех типов сущностей")
    @Test
    void shouldCreateTemporaryTables() {
        assertThat(Arrays.stream(EntityKind.values()).map(JdbcExportIdMappingStore::tableName))
                .allMatch(this::tableExists);
    }

    @DisplayName("должен возвращать только сохранённые соответствия нужного типа")
    @Test
    void shouldReturnOnlyStoredMappingsOfKind() {
        store.putAll(EntityKind.AUTHOR, Map.of("a1", 1L, "a2", 2L));
        store.putAll(EntityKind.GENRE, Map.of("g1", 10L));

        assertThat(store.getAll(EntityKind.AUTHOR, List.of("a1", "a2", "a3")))
                .containsExactlyInAnyOrderEntriesOf(Map.of("a1", 1L, "a2", 2L));
        assertThat(store.getAll(EntityKind.GENRE, List.of("g1"))).containsExactlyEntriesOf(Map.of("g1", 10L));
        assertThat(store.getAll(EntityKind.BOOK, List.of("a1"))).isEmpty();
        assertThat(store.getAll(EntityKind.BOOK, List.of())).isEmpty();
    }

    @DisplayName("должен очищать данные при повторной подготовке")
    @Test
    void shouldResetDataOnPrepare() {
        store.putAll(EntityKind.AUTHOR, Map.of("a1", 1L));

        store.prepare();

        assertThat(store.getAll(EntityKind.AUTHOR, List.of("a1"))).isEmpty();
    }

    @DisplayName("должен удалять временные таблицы")
    @Test
    void shouldDropTemporaryTablesOnCleanup() {
        store.cleanup();

        assertThat(Arrays.stream(EntityKind.values()).map(JdbcExportIdMappingStore::tableName))
                .noneMatch(this::tableExists);
    }

    private boolean tableExists(String table) {
        var count = jdbc.getJdbcTemplate().queryForObject(
                "select count(*) from information_schema.tables where lower(table_name) = ?",
                Integer.class, table);
        return count != null && count > 0;
    }
}
