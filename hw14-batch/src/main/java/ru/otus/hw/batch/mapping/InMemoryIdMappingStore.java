package ru.otus.hw.batch.mapping;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryIdMappingStore<S, T> implements IdMappingStore<S, T> {

    private final Map<EntityKind, Map<S, T>> mappings = new ConcurrentHashMap<>();

    @Override
    public void prepare() {
        mappings.clear();
    }

    @Override
    public void putAll(EntityKind kind, Map<S, T> ids) {
        mappingsOf(kind).putAll(ids);
    }

    @Override
    public Map<S, T> getAll(EntityKind kind, Collection<S> sourceIds) {
        var kindMappings = mappingsOf(kind);
        var result = new HashMap<S, T>();
        for (S sourceId : sourceIds) {
            var targetId = kindMappings.get(sourceId);
            if (targetId != null) {
                result.put(sourceId, targetId);
            }
        }
        return result;
    }

    @Override
    public void cleanup() {
        mappings.clear();
    }

    private Map<S, T> mappingsOf(EntityKind kind) {
        return mappings.computeIfAbsent(kind, k -> new ConcurrentHashMap<>());
    }
}
