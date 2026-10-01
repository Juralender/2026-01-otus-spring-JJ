package ru.otus.hw.batch.mapping;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class JdbcExportIdMappingStore implements IdMappingStore<String, Long> {

    private final NamedParameterJdbcTemplate jdbc;

    public static String tableName(EntityKind kind) {
        return "tmp_export_%s_ids".formatted(kind.getStorageName());
    }

    @Override
    public void prepare() {
        for (EntityKind kind : EntityKind.values()) {
            var table = tableName(kind);
            jdbc.getJdbcTemplate().execute("drop table if exists " + table);
            jdbc.getJdbcTemplate().execute("create table " + table
                    + " (mongo_id varchar(64) primary key, sql_id bigint not null)");
        }
    }

    @Override
    public void putAll(EntityKind kind, Map<String, Long> ids) {
        var params = ids.entrySet().stream()
                .map(e -> new MapSqlParameterSource()
                        .addValue("mongoId", e.getKey())
                        .addValue("sqlId", e.getValue()))
                .toArray(SqlParameterSource[]::new);
        jdbc.batchUpdate("insert into " + tableName(kind) + " (mongo_id, sql_id) values (:mongoId, :sqlId)",
                params);
    }

    @Override
    public Map<String, Long> getAll(EntityKind kind, Collection<String> sourceIds) {
        var result = new HashMap<String, Long>();
        if (sourceIds.isEmpty()) {
            return result;
        }
        jdbc.query("select mongo_id, sql_id from " + tableName(kind) + " where mongo_id in (:ids)",
                Map.of("ids", sourceIds),
                rs -> {
                    result.put(rs.getString("mongo_id"), rs.getLong("sql_id"));
                });
        return result;
    }

    @Override
    public void cleanup() {
        for (EntityKind kind : EntityKind.values()) {
            jdbc.getJdbcTemplate().execute("drop table if exists " + tableName(kind));
        }
    }
}
