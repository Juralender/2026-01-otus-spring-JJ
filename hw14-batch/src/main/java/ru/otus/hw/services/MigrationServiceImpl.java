package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;
import ru.otus.hw.batch.MigrationMode;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static ru.otus.hw.batch.exporting.ExportJobConfig.EXPORT_DB_JOB;
import static ru.otus.hw.batch.exporting.ExportJobConfig.EXPORT_RAM_JOB;
import static ru.otus.hw.batch.importing.ImportJobConfig.IMPORT_DB_JOB;
import static ru.otus.hw.batch.importing.ImportJobConfig.IMPORT_RAM_JOB;

@RequiredArgsConstructor
@Service
public class MigrationServiceImpl implements MigrationService {

    private static final Set<BatchStatus> RESTARTABLE_STATUSES = Set.of(BatchStatus.FAILED, BatchStatus.STOPPED);

    private final JobLauncher jobLauncher;

    private final JobExplorer jobExplorer;

    private final Map<String, Job> jobs;

    @Override
    public JobExecution exportToSql(MigrationMode mode) {
        return run(job(mode == MigrationMode.RAM ? EXPORT_RAM_JOB : EXPORT_DB_JOB), new JobParametersBuilder()
                .addString("runId", UUID.randomUUID().toString())
                .toJobParameters());
    }

    @Override
    public JobExecution importToMongo(MigrationMode mode) {
        return run(job(mode == MigrationMode.RAM ? IMPORT_RAM_JOB : IMPORT_DB_JOB), new JobParametersBuilder()
                .addString("runId", UUID.randomUUID().toString())
                .toJobParameters());
    }

    @Override
    public JobExecution restart(long executionId) {
        var execution = jobExplorer.getJobExecution(executionId);
        if (execution == null) {
            throw new IllegalArgumentException("Job execution with id %d not found".formatted(executionId));
        }
        var job = job(execution.getJobInstance().getJobName());
        if (!job.isRestartable()) {
            throw new IllegalStateException("Job %s works in RAM mode and can't be restarted, run it again"
                    .formatted(job.getName()));
        }
        if (!RESTARTABLE_STATUSES.contains(execution.getStatus())) {
            throw new IllegalStateException("Job execution %d has status %s, only failed or stopped can be restarted"
                    .formatted(executionId, execution.getStatus()));
        }
        return run(job, execution.getJobParameters());
    }

    private Job job(String name) {
        var job = jobs.get(name);
        if (job == null) {
            throw new IllegalArgumentException("Job %s not found".formatted(name));
        }
        return job;
    }

    private JobExecution run(Job job, JobParameters parameters) {
        try {
            return jobLauncher.run(job, parameters);
        } catch (JobExecutionException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
