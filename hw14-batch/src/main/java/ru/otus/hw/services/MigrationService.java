package ru.otus.hw.services;

import org.springframework.batch.core.JobExecution;
import ru.otus.hw.batch.MigrationMode;

public interface MigrationService {

    JobExecution exportToSql(MigrationMode mode);

    JobExecution importToMongo(MigrationMode mode);

    JobExecution restart(long executionId);
}
