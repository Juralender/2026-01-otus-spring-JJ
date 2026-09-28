package ru.otus.hw.batch.exporting;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import ru.otus.hw.batch.mapping.EntityKind;
import ru.otus.hw.batch.mapping.IdMappingStore;

import java.util.function.Function;

@RequiredArgsConstructor
public class SimpleExportWriter<T> implements ItemWriter<T> {

    private final JdbcBatchInserter inserter;

    private final IdMappingStore<String, Long> idStore;

    private final EntityKind kind;

    private final String sql;

    private final Function<T, String> sourceId;

    private final Function<T, SqlParameterSource> params;

    @Override
    public void write(Chunk<? extends T> chunk) {
        idStore.putAll(kind, inserter.insert(sql, chunk.getItems(), sourceId, params));
    }
}
