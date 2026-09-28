package ru.otus.hw.batch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import ru.otus.hw.batch.MigrationMode;

@Component
@RequiredArgsConstructor
public class MigrationStepFactory {

    private final JobRepository jobRepository;

    private final PlatformTransactionManager transactionManager;

    private final MigrationProperties properties;

    private final TaskExecutor migrationTaskExecutor;

    public Step taskletStep(String name, Runnable action) {
        return new StepBuilder(name, jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    action.run();
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    public <T> Step chunkStep(String name, MigrationMode mode, ItemStreamReader<T> reader, ItemWriter<T> writer) {
        var builder = new StepBuilder(name, jobRepository)
                .<T, T>chunk(properties.chunkSize(), transactionManager)
                .writer(writer);
        if (mode == MigrationMode.RAM) {
            var synchronizedReader = new SynchronizedItemStreamReader<T>();
            synchronizedReader.setDelegate(reader);
            return builder.reader(synchronizedReader).taskExecutor(migrationTaskExecutor).build();
        }
        return builder.reader(reader).build();
    }

    public Job job(String name, MigrationMode mode, MigrationSteps steps) {
        var jobBuilder = new JobBuilder(name, jobRepository);
        if (mode == MigrationMode.RAM) {
            jobBuilder.preventRestart();
        }
        return jobBuilder
                .start(flow(steps.prepare()))
                .next(parallel(name + "AuthorsAndGenres", steps.authors(), steps.genres()))
                .next(steps.books())
                .next(steps.comments())
                .next(steps.cleanup())
                .end()
                .build();
    }

    private Flow parallel(String name, Step first, Step second) {
        return new FlowBuilder<SimpleFlow>(name)
                .split(new SimpleAsyncTaskExecutor(name + "-"))
                .add(flow(first), flow(second))
                .build();
    }

    private Flow flow(Step step) {
        return new FlowBuilder<SimpleFlow>(step.getName() + "Flow").start(step).build();
    }
}
