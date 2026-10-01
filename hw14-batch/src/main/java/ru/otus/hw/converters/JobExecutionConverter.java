package ru.otus.hw.converters;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.stream.Collectors;

@Component
public class JobExecutionConverter {

    public String executionToString(JobExecution execution) {
        var result = new StringBuilder("Job %s, execution id %d: %s".formatted(
                execution.getJobInstance().getJobName(), execution.getId(), execution.getStatus()));
        execution.getStepExecutions().stream()
                .sorted(Comparator.comparing(StepExecution::getId))
                .map(this::stepToString)
                .forEach(step -> result.append(System.lineSeparator()).append(step));
        if (execution.getStatus() == BatchStatus.FAILED) {
            result.append(System.lineSeparator()).append("Errors: ").append(execution.getAllFailureExceptions()
                    .stream().map(Throwable::getMessage).collect(Collectors.joining("; ")));
        }
        return result.toString();
    }

    private String stepToString(StepExecution step) {
        return "  %s: %s, read %d, written %d".formatted(
                step.getStepName(), step.getStatus(), step.getReadCount(), step.getWriteCount());
    }
}
