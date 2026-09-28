package ru.otus.hw.batch.mapping;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@RequiredArgsConstructor
public class MongoImportIdMappingStore implements IdMappingStore<Long, String> {

    private static final String SQL_ID = "sqlId";

    private final MongoTemplate mongoTemplate;

    public static String collectionName(EntityKind kind) {
        return "tmp_import_%s_ids".formatted(kind.getStorageName());
    }

    @Override
    public void prepare() {
        for (EntityKind kind : EntityKind.values()) {
            var collection = collectionName(kind);
            mongoTemplate.dropCollection(collection);
            mongoTemplate.createCollection(collection);
            mongoTemplate.indexOps(collection).createIndex(new Index(SQL_ID, Sort.Direction.ASC).unique());
        }
    }

    @Override
    public void putAll(EntityKind kind, Map<Long, String> ids) {
        if (ids.isEmpty()) {
            return;
        }
        var bulk = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, collectionName(kind));
        ids.forEach((sqlId, mongoId) ->
                bulk.upsert(query(where(SQL_ID).is(sqlId)), new Update().setOnInsert("_id", mongoId)));
        bulk.execute();
    }

    @Override
    public Map<Long, String> getAll(EntityKind kind, Collection<Long> sourceIds) {
        var result = new HashMap<Long, String>();
        if (sourceIds.isEmpty()) {
            return result;
        }
        mongoTemplate.find(query(where(SQL_ID).in(sourceIds)), Document.class, collectionName(kind))
                .forEach(doc -> result.put(((Number) doc.get(SQL_ID)).longValue(), doc.get("_id").toString()));
        return result;
    }

    @Override
    public void cleanup() {
        for (EntityKind kind : EntityKind.values()) {
            mongoTemplate.dropCollection(collectionName(kind));
        }
    }
}
