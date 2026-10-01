package ru.otus.hw.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import ru.otus.hw.batch.MigrationMode;
import ru.otus.hw.converters.JobExecutionConverter;
import ru.otus.hw.services.MigrationService;
import ru.otus.hw.services.SqlDataFileService;
import ru.otus.hw.services.StorageStatsService;

import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("Команды миграции")
class MigrationCommandsTest {

    private MigrationService migrationService;

    private StorageStatsService storageStatsService;

    private SqlDataFileService sqlDataFileService;

    private MigrationCommands commands;

    @BeforeEach
    void setUp() {
        migrationService = mock(MigrationService.class);
        storageStatsService = mock(StorageStatsService.class);
        sqlDataFileService = mock(SqlDataFileService.class);
        var converter = mock(JobExecutionConverter.class);
        when(converter.executionToString(any())).thenReturn("result");
        commands = new MigrationCommands(migrationService, storageStatsService, sqlDataFileService, converter);
    }

    @DisplayName("должен запускать экспорт в выбранном режиме")
    @Test
    void shouldRunExportInSelectedMode() {
        when(migrationService.exportToSql(MigrationMode.RAM)).thenReturn(new JobExecution(1L));

        assertThat(commands.exportToSql("ram", null)).isEqualTo("result");
        verify(migrationService).exportToSql(MigrationMode.RAM);
        verifyNoInteractions(sqlDataFileService);
    }

    @DisplayName("должен сохранять данные в файл после успешного экспорта")
    @Test
    void shouldSaveFileAfterCompletedExport() {
        when(migrationService.exportToSql(MigrationMode.DB)).thenReturn(execution(BatchStatus.COMPLETED));

        var result = commands.exportToSql("DB", "library.sql");

        verify(sqlDataFileService).save(Path.of("library.sql").toAbsolutePath());
        assertThat(result).contains("saved to").contains("library.sql");
    }

    @DisplayName("не должен сохранять файл после неуспешного экспорта")
    @Test
    void shouldNotSaveFileAfterFailedExport() {
        when(migrationService.exportToSql(MigrationMode.DB)).thenReturn(execution(BatchStatus.FAILED));

        var result = commands.exportToSql("DB", "library.sql");

        verifyNoInteractions(sqlDataFileService);
        assertThat(result).contains("not saved");
    }

    @DisplayName("должен загружать данные из файла перед импортом")
    @Test
    void shouldRestoreFileBeforeImport() {
        when(migrationService.importToMongo(MigrationMode.RAM)).thenReturn(execution(BatchStatus.COMPLETED));

        var result = commands.importToMongo("RAM", "library.sql");

        var order = inOrder(sqlDataFileService, migrationService);
        order.verify(sqlDataFileService).restore(Path.of("library.sql").toAbsolutePath());
        order.verify(migrationService).importToMongo(MigrationMode.RAM);
        assertThat(result).contains("loaded from").contains("library.sql");
    }

    @DisplayName("не должен запускать импорт, если файл не загружен")
    @Test
    void shouldNotImportWhenFileRestoreFails() {
        doThrow(new IllegalArgumentException("not found")).when(sqlDataFileService).restore(any());

        assertThatThrownBy(() -> commands.importToMongo("DB", "missing.sql"))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(migrationService);
    }

    @DisplayName("должен запускать импорт в выбранном режиме")
    @Test
    void shouldRunImportInSelectedMode() {
        when(migrationService.importToMongo(MigrationMode.DB)).thenReturn(new JobExecution(1L));

        assertThat(commands.importToMongo("DB", null)).isEqualTo("result");
        verify(migrationService).importToMongo(MigrationMode.DB);
    }

    @DisplayName("должен перезапускать задачу по идентификатору запуска")
    @Test
    void shouldRestartByExecutionId() {
        when(migrationService.restart(5L)).thenReturn(new JobExecution(6L));

        assertThat(commands.restart(5L)).isEqualTo("result");
        verify(migrationService).restart(5L);
    }

    @DisplayName("не должен запускать миграцию с неизвестным режимом")
    @Test
    void shouldRejectUnknownMode() {
        assertThatThrownBy(() -> commands.exportToSql("disk", null)).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(migrationService);
    }

    private static JobExecution execution(BatchStatus status) {
        var execution = new JobExecution(new JobInstance(1L, "job"), 1L, new JobParameters());
        execution.setStatus(status);
        return execution;
    }

    @DisplayName("должен показывать количество записей в обоих хранилищах")
    @Test
    void shouldShowStats() {
        when(storageStatsService.relationalCounts()).thenReturn(Map.of("authors", 3L));
        when(storageStatsService.mongoCounts()).thenReturn(Map.of("authors", 4L));

        assertThat(commands.stats()).contains("Relational DB: {authors=3}").contains("MongoDB: {authors=4}");
    }
}
