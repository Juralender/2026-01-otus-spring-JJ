package ru.otus.hw.batch.mapping;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Хранилище соответствия идентификаторов импорта во временных коллекциях")
@DataMongoTest
class MongoImportIdMappingStoreTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    private MongoImportIdMappingStore store;

    @BeforeEach
    void setUp() {
        store = new MongoImportIdMappingStore(mongoTemplate);
        store.prepare();
    }

    @AfterEach
    void tearDown() {
        store.cleanup();
    }

    @DisplayName("должен создавать временные коллекции для всех типов сущностей")
    @Test
    void shouldCreateTemporaryCollections() {
        assertThat(Arrays.stream(EntityKind.values()).map(MongoImportIdMappingStore::collectionName))
                .allMatch(mongoTemplate::collectionExists);
    }

    @DisplayName("должен возвращать только сохранённые соответствия нужного типа")
    @Test
    void shouldReturnOnlyStoredMappingsOfKind() {
        store.putAll(EntityKind.AUTHOR, Map.of(1L, "a1", 2L, "a2"));
        store.putAll(EntityKind.GENRE, Map.of(1L, "g1"));

        assertThat(store.getAll(EntityKind.AUTHOR, List.of(1L, 2L, 3L)))
                .containsExactlyInAnyOrderEntriesOf(Map.of(1L, "a1", 2L, "a2"));
        assertThat(store.getAll(EntityKind.GENRE, List.of(1L))).containsExactlyEntriesOf(Map.of(1L, "g1"));
        assertThat(store.getAll(EntityKind.BOOK, List.of(1L))).isEmpty();
        assertThat(store.getAll(EntityKind.BOOK, List.of())).isEmpty();
    }

    @DisplayName("должен возвращать идентификаторы в формате ObjectId как строки")
    @Test
    void shouldReturnObjectIdsAsStrings() {
        var mongoId = new ObjectId().toHexString();
        store.putAll(EntityKind.BOOK, Map.of(7L, mongoId));

        assertThat(store.getAll(EntityKind.BOOK, List.of(7L))).containsExactlyEntriesOf(Map.of(7L, mongoId));
    }

    @DisplayName("должен сохранять первое соответствие при повторной записи")
    @Test
    void shouldKeepFirstMappingOnRepeatedWrite() {
        store.putAll(EntityKind.BOOK, Map.of(1L, "first"));

        store.putAll(EntityKind.BOOK, Map.of(1L, "second"));

        assertThat(store.getAll(EntityKind.BOOK, List.of(1L))).containsExactlyEntriesOf(Map.of(1L, "first"));
    }

    @DisplayName("должен удалять временные коллекции")
    @Test
    void shouldDropTemporaryCollectionsOnCleanup() {
        store.cleanup();

        assertThat(Arrays.stream(EntityKind.values()).map(MongoImportIdMappingStore::collectionName))
                .noneMatch(mongoTemplate::collectionExists);
    }
}
