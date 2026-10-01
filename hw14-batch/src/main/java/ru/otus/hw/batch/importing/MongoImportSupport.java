package ru.otus.hw.batch.importing;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import ru.otus.hw.batch.mapping.EntityKind;
import ru.otus.hw.batch.mapping.IdMappingStore;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Component
@RequiredArgsConstructor
public class MongoImportSupport {

    private final MongoTemplate mongoTemplate;

    public Map<Long, String> assignIds(IdMappingStore<Long, String> idStore, EntityKind kind,
                                       Collection<Long> sqlIds) {
        var ids = new HashMap<>(idStore.getAll(kind, sqlIds));
        var created = new HashMap<Long, String>();
        for (Long sqlId : sqlIds) {
            if (!ids.containsKey(sqlId)) {
                created.put(sqlId, new ObjectId().toHexString());
            }
        }
        idStore.putAll(kind, created);
        ids.putAll(created);
        return ids;
    }

    public <T> void upsertAll(Class<T> type, List<T> documents, Function<T, String> id) {
        if (documents.isEmpty()) {
            return;
        }
        var bulk = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, type);
        documents.forEach(document -> bulk.replaceOne(query(where("_id").is(new ObjectId(id.apply(document)))),
                document, FindAndReplaceOptions.options().upsert()));
        bulk.execute();
    }
}
