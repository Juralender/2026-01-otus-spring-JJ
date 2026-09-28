package ru.otus.hw.batch.config;

import org.springframework.batch.core.Step;

public record MigrationSteps(Step prepare, Step authors, Step genres, Step books, Step comments, Step cleanup) {
}
