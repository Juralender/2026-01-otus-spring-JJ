package ru.otus.hw.batch.importing;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import ru.otus.hw.batch.mapping.EntityKind;
import ru.otus.hw.batch.mapping.IdMappingStore;

import java.util.function.BiFunction;
import java.util.function.Function;

@RequiredArgsConstructor
public class SimpleImportWriter<R, T> implements ItemWriter<R> {

    private final MongoImportSupport support;

    private final IdMappingStore<Long, String> idStore;

    private final EntityKind kind;

    private final Class<T> type;

    private final Function<R, Long> sqlId;

    private final BiFunction<R, String, T> toDocument;

    private final Function<T, String> documentId;

    @Override
    public void write(Chunk<? extends R> chunk) {
        var rows = chunk.getItems();
        var ids = support.assignIds(idStore, kind, rows.stream().map(sqlId).toList());
        support.upsertAll(type, rows.stream()
                .map(row -> toDocument.apply(row, ids.get(sqlId.apply(row))))
                .toList(), documentId);
    }
}
