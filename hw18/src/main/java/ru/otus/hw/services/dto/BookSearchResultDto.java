package ru.otus.hw.services.dto;

import java.util.List;

public record BookSearchResultDto(String query, long total, List<BookDto> books) {
}
