package ru.otus.hw.batch.importing;

public record BookRow(long id, String title, Long authorId, String authorFullName) {
}
