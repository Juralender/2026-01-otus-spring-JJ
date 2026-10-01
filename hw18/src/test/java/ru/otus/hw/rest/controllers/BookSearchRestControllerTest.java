package ru.otus.hw.rest.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.exceptions.ExternalServiceException;
import ru.otus.hw.services.BookSearchService;
import ru.otus.hw.services.dto.BookDto;
import ru.otus.hw.services.dto.BookSearchResultDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("REST контроллер поиска книг")
@WebMvcTest(BookSearchRestController.class)
class BookSearchRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private BookSearchService bookSearchService;

    @DisplayName("должен возвращать результаты поиска")
    @Test
    void shouldReturnSearchResult() throws Exception {
        given(bookSearchService.search("dune", 2)).willReturn(new BookSearchResultDto("dune", 42, List.of(
                new BookDto("/works/OL1W", "Dune", List.of("Frank Herbert"), 1965, "cover"))));

        mvc.perform(get("/api/books/search").param("q", " dune ").param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("dune"))
                .andExpect(jsonPath("$.total").value(42))
                .andExpect(jsonPath("$.books[0].title").value("Dune"))
                .andExpect(jsonPath("$.books[0].authors[0]").value("Frank Herbert"))
                .andExpect(jsonPath("$.books[0].firstPublishYear").value(1965));
    }

    @DisplayName("должен использовать лимит 10 по умолчанию")
    @Test
    void shouldUseDefaultLimit() throws Exception {
        given(bookSearchService.search("dune", 10)).willReturn(new BookSearchResultDto("dune", 0, List.of()));

        mvc.perform(get("/api/books/search").param("q", "dune"))
                .andExpect(status().isOk());

        verify(bookSearchService).search("dune", 10);
    }

    @DisplayName("должен возвращать 400 при некорректных параметрах")
    @Test
    void shouldReturnBadRequestForInvalidParams() throws Exception {
        mvc.perform(get("/api/books/search")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/books/search").param("q", " ")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/books/search").param("q", "dune").param("limit", "0"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/books/search").param("q", "dune").param("limit", "101"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/books/search").param("q", "dune").param("limit", "abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookSearchService);
    }

    @DisplayName("должен возвращать 502, если Open Library недоступна")
    @Test
    void shouldReturnBadGatewayWhenServiceFails() throws Exception {
        given(bookSearchService.search(anyString(), anyInt()))
                .willThrow(new ExternalServiceException("Open Library is unavailable", null));

        mvc.perform(get("/api/books/search").param("q", "dune"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.detail").value("Open Library is unavailable"));
    }
}
