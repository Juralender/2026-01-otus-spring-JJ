package ru.otus.hw.batch.exporting;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JdbcBatchInserter {

    private final NamedParameterJdbcTemplate jdbc;

    public <T> Map<String, Long> insert(String sql, List<? extends T> items, Function<T, String> sourceId,
                                        Function<T, SqlParameterSource> params) {
        var keyHolder = new GeneratedKeyHolder();
        jdbc.batchUpdate(sql, items.stream().map(params).toArray(SqlParameterSource[]::new), keyHolder,
                new String[]{"id"});
        var keys = keyHolder.getKeyList();
        if (keys.size() != items.size()) {
            throw new IllegalStateException("Expected %d generated keys but got %d"
                    .formatted(items.size(), keys.size()));
        }
        var ids = new HashMap<String, Long>();
        for (int i = 0; i < items.size(); i++) {
            ids.put(sourceId.apply(items.get(i)), ((Number) keys.get(i).values().iterator().next()).longValue());
        }
        return ids;
    }

    public void insert(String sql, List<SqlParameterSource> params) {
        if (!params.isEmpty()) {
            jdbc.batchUpdate(sql, params.toArray(SqlParameterSource[]::new));
        }
    }
}
