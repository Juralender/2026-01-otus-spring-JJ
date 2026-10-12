package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.client.OpenLibraryClient;
import ru.otus.hw.client.dto.OpenLibraryDoc;
import ru.otus.hw.config.OpenLibraryProperties;
import ru.otus.hw.services.dto.BookDto;
import ru.otus.hw.services.dto.BookSearchResultDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookSearchServiceImpl implements BookSearchService {

    private final OpenLibraryClient openLibraryClient;

    private final OpenLibraryProperties properties;

    @Override
    public BookSearchResultDto search(String query, int limit) {
        var response = openLibraryClient.search(query, limit);
        if (response == null || response.docs() == null) {
            return new BookSearchResultDto(query, 0, List.of());
        }
        var books = response.docs().stream()
                .map(this::toBookDto)
                .toList();
        return new BookSearchResultDto(query, response.numFound(), books);
    }

    private BookDto toBookDto(OpenLibraryDoc doc) {
        var authors = doc.authorNames() == null ? List.<String>of() : doc.authorNames();
        var coverUrl = doc.coverId() == null
                ? null
                : "%s/b/id/%d-M.jpg".formatted(properties.coversUrl(), doc.coverId());
        return new BookDto(doc.key(), doc.title(), authors, doc.firstPublishYear(), coverUrl);
    }
}
