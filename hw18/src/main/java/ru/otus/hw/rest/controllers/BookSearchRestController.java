package ru.otus.hw.rest.controllers;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.services.BookSearchService;
import ru.otus.hw.services.dto.BookSearchResultDto;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookSearchRestController {

    private final BookSearchService bookSearchService;

    @GetMapping("/search")
    public BookSearchResultDto search(@RequestParam("q") @NotBlank String query,
                                      @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        return bookSearchService.search(query.strip(), limit);
    }
}
