package ru.otus.hw.client;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.otus.hw.client.dto.OpenLibrarySearchResponse;
import ru.otus.hw.config.OpenLibraryProperties;
import ru.otus.hw.exceptions.ExternalServiceException;

@Slf4j
@Component
public class OpenLibraryClient {

    private static final String RESILIENCE_INSTANCE = "openLibrary";

    private static final String SEARCH_FIELDS = "key,title,author_name,first_publish_year,cover_i";

    private final RestClient restClient;

    public OpenLibraryClient(RestClient.Builder builder, OpenLibraryProperties properties) {
        this.restClient = builder
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.USER_AGENT, properties.userAgent())
                .build();
    }

    @Retry(name = RESILIENCE_INSTANCE, fallbackMethod = "searchFallback")
    @CircuitBreaker(name = RESILIENCE_INSTANCE)
    public OpenLibrarySearchResponse search(String query, int limit) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/search.json")
                        .queryParam("q", query)
                        .queryParam("limit", limit)
                        .queryParam("fields", SEARCH_FIELDS)
                        .build())
                .retrieve()
                .body(OpenLibrarySearchResponse.class);
    }

    private OpenLibrarySearchResponse searchFallback(String query, int limit, CallNotPermittedException e) {
        log.warn("Open Library circuit breaker is open, search '{}' rejected", query);
        throw new ExternalServiceException("Open Library is temporarily unavailable, try again later", e);
    }

    private OpenLibrarySearchResponse searchFallback(String query, int limit, Throwable e) {
        log.warn("Open Library search '{}' failed: {}", query, e.toString());
        throw new ExternalServiceException("Open Library is unavailable", e);
    }
}
