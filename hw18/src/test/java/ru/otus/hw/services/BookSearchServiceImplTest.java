package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.hw.client.OpenLibraryClient;
import ru.otus.hw.client.dto.OpenLibraryDoc;
import ru.otus.hw.client.dto.OpenLibrarySearchResponse;
import ru.otus.hw.config.OpenLibraryProperties;
import ru.otus.hw.services.dto.BookDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@DisplayName("Сервис поиска книг")
class BookSearchServiceImplTest {

    private final OpenLibraryClient client = mock(OpenLibraryClient.class);

    private final BookSearchService service = new BookSearchServiceImpl(client,
            new OpenLibraryProperties("https://openlibrary.test", "https://covers.test", "agent"));

    @DisplayName("должен преобразовывать ответ Open Library в книги")
    @Test
    void shouldMapResponseToBooks() {
        given(client.search("dune", 2)).willReturn(new OpenLibrarySearchResponse(42, List.of(
                new OpenLibraryDoc("/works/OL1W", "Dune", List.of("Frank Herbert"), 1965, 123L),
                new OpenLibraryDoc("/works/OL2W", "No authors", null, null, null))));

        var result = service.search("dune", 2);

        assertThat(result.query()).isEqualTo("dune");
        assertThat(result.total()).isEqualTo(42);
        assertThat(result.books()).containsExactly(
                new BookDto("/works/OL1W", "Dune", List.of("Frank Herbert"), 1965,
                        "https://covers.test/b/id/123-M.jpg"),
                new BookDto("/works/OL2W", "No authors", List.of(), null, null));
    }

    @DisplayName("должен возвращать пустой результат, если документов нет")
    @Test
    void shouldReturnEmptyResultWhenNoDocs() {
        given(client.search("nothing", 10)).willReturn(new OpenLibrarySearchResponse(0, null));

        var result = service.search("nothing", 10);

        assertThat(result.total()).isZero();
        assertThat(result.books()).isEmpty();
    }
}
