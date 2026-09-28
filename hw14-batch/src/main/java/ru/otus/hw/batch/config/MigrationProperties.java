package ru.otus.hw.batch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("migration")
public record MigrationProperties(@DefaultValue("100") int chunkSize, @DefaultValue("4") int threads) {
}
