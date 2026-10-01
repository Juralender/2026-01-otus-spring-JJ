package ru.otus.hw.client;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ru.otus.hw.client.dto.OpenLibrarySearchResponse;
import ru.otus.hw.config.OpenLibraryProperties;
import ru.otus.hw.exceptions.ExternalServiceException;

@Component
public class OpenLibraryClient {

    private static final String SEARCH_FIELDS = "key,title,author_name,first_publish_year,cover_i";

    private final RestClient restClient;

    public OpenLibraryClient(RestClient.Builder builder, OpenLibraryProperties properties) {
        this.restClient = builder
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.USER_AGENT, properties.userAgent())
                .build();
    }

    public OpenLibrarySearchResponse search(String query, int limit) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/search.json")
                            .queryParam("q", query)
                            .queryParam("limit", limit)
                            .queryParam("fields", SEARCH_FIELDS)
                            .build())
                    .retrieve()
                    .body(OpenLibrarySearchResponse.class);
        } catch (RestClientException e) {
            throw new ExternalServiceException("Open Library is unavailable", e);
        }
    }
}
