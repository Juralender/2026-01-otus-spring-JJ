package ru.otus.hw.batch.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.otus.hw.batch.mapping.IdMappingStore;
import ru.otus.hw.batch.mapping.InMemoryIdMappingStore;
import ru.otus.hw.batch.mapping.JdbcExportIdMappingStore;
import ru.otus.hw.batch.mapping.MongoImportIdMappingStore;

@Configuration
@EnableConfigurationProperties(MigrationProperties.class)
public class MigrationInfrastructureConfig {

    @Bean
    public TaskExecutor migrationTaskExecutor(MigrationProperties properties) {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.threads());
        executor.setMaxPoolSize(properties.threads());
        executor.setThreadNamePrefix("migration-");
        executor.setDaemon(true);
        return executor;
    }

    @Bean
    public IdMappingStore<String, Long> ramExportIdMappingStore() {
        return new InMemoryIdMappingStore<>();
    }

    @Bean
    public IdMappingStore<String, Long> dbExportIdMappingStore(NamedParameterJdbcTemplate jdbc) {
        return new JdbcExportIdMappingStore(jdbc);
    }

    @Bean
    public IdMappingStore<Long, String> ramImportIdMappingStore() {
        return new InMemoryIdMappingStore<>();
    }

    @Bean
    public IdMappingStore<Long, String> dbImportIdMappingStore(MongoTemplate mongoTemplate) {
        return new MongoImportIdMappingStore(mongoTemplate);
    }
}
