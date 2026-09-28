package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.otus.hw.batch.MigrationMode;
import ru.otus.hw.converters.JobExecutionConverter;
import ru.otus.hw.services.MigrationService;
import ru.otus.hw.services.SqlDataFileService;
import ru.otus.hw.services.StorageStatsService;

import java.nio.file.Path;

@RequiredArgsConstructor
@ShellComponent
public class MigrationCommands {

    private final MigrationService migrationService;

    private final StorageStatsService storageStatsService;

    private final SqlDataFileService sqlDataFileService;

    private final JobExecutionConverter jobExecutionConverter;

    @ShellMethod(value = "Export data from MongoDB to relational DB, mode: RAM or DB, "
            + "file: optional path to save relational data after export", key = "export")
    public String exportToSql(@ShellOption(defaultValue = "DB") String mode,
                              @ShellOption(defaultValue = ShellOption.NULL) String file) {
        var execution = migrationService.exportToSql(MigrationMode.of(mode));
        var result = jobExecutionConverter.executionToString(execution);
        if (file == null) {
            return result;
        }
        var path = Path.of(file).toAbsolutePath();
        if (execution.getStatus() != BatchStatus.COMPLETED) {
            return result + System.lineSeparator() + "Relational data not saved to %s: export failed".formatted(path);
        }
        sqlDataFileService.save(path);
        return result + System.lineSeparator() + "Relational data saved to " + path;
    }

    @ShellMethod(value = "Import data from relational DB to MongoDB, mode: RAM or DB, "
            + "file: optional path to load relational data from before import", key = "import")
    public String importToMongo(@ShellOption(defaultValue = "DB") String mode,
                                @ShellOption(defaultValue = ShellOption.NULL) String file) {
        var migrationMode = MigrationMode.of(mode);
        if (file == null) {
            return jobExecutionConverter.executionToString(migrationService.importToMongo(migrationMode));
        }
        var path = Path.of(file).toAbsolutePath();
        sqlDataFileService.restore(path);
        return "Relational data loaded from " + path + System.lineSeparator()
                + jobExecutionConverter.executionToString(migrationService.importToMongo(migrationMode));
    }

    @ShellMethod(value = "Restart failed DB mode migration by job execution id", key = "restart")
    public String restart(@ShellOption("--id") long executionId) {
        return jobExecutionConverter.executionToString(migrationService.restart(executionId));
    }

    @ShellMethod(value = "Show records count in relational DB and MongoDB", key = "stats")
    public String stats() {
        return "Relational DB: " + storageStatsService.relationalCounts()
                + System.lineSeparator()
                + "MongoDB: " + storageStatsService.mongoCounts();
    }
}
