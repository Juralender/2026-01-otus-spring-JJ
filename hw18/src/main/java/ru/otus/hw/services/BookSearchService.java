package ru.otus.hw.services;

import ru.otus.hw.services.dto.BookSearchResultDto;

public interface BookSearchService {

    BookSearchResultDto search(String query, int limit);
}
