package ru.otus.hw.services.dto;

import java.util.List;

public record BookDto(String key, String title, List<String> authors, Integer firstPublishYear, String coverUrl) {
}
