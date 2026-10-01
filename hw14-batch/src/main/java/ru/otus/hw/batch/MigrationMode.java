package ru.otus.hw.batch;

import java.util.Arrays;

public enum MigrationMode {
    RAM,
    DB;

    public static MigrationMode of(String value) {
        return Arrays.stream(values())
                .filter(mode -> mode.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown mode '%s', expected one of %s".formatted(value, Arrays.toString(values()))));
    }
}
