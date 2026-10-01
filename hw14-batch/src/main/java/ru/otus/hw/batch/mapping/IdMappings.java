package ru.otus.hw.batch.mapping;

import java.util.Map;

public final class IdMappings {

    private IdMappings() {
    }

    public static <S, T> T require(Map<S, T> mappings, S sourceId, EntityKind kind) {
        if (sourceId == null) {
            return null;
        }
        var targetId = mappings.get(sourceId);
        if (targetId == null) {
            throw new IllegalStateException("No target id for %s with source id %s".formatted(kind, sourceId));
        }
        return targetId;
    }
}
