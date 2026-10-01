package ru.otus.hw.batch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Режим миграции")
class MigrationModeTest {

    @DisplayName("должен определяться без учёта регистра")
    @Test
    void shouldParseIgnoringCase() {
        assertThat(MigrationMode.of("ram")).isEqualTo(MigrationMode.RAM);
        assertThat(MigrationMode.of("Db")).isEqualTo(MigrationMode.DB);
    }

    @DisplayName("должен сообщать о неизвестном режиме")
    @Test
    void shouldRejectUnknownMode() {
        assertThatThrownBy(() -> MigrationMode.of("disk"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("disk");
    }
}
