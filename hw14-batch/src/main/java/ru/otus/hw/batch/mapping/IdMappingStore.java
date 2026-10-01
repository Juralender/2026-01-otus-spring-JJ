package ru.otus.hw.batch.mapping;

import java.util.Collection;
import java.util.Map;

public interface IdMappingStore<S, T> {

    void prepare();

    void putAll(EntityKind kind, Map<S, T> ids);

    Map<S, T> getAll(EntityKind kind, Collection<S> sourceIds);

    void cleanup();
}
