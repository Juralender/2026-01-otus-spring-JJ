package ru.otus.hw.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import ru.otus.hw.client.dto.OpenLibraryDoc;
import ru.otus.hw.config.OpenLibraryProperties;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;

@DisplayName("Клиент Open Library")
@RestClientTest(OpenLibraryClient.class)
@EnableConfigurationProperties(OpenLibraryProperties.class)
@ActiveProfiles("test")
class OpenLibraryClientTest {

    private static final String SEARCH_URL = "https://openlibrary.test/search.json?q=dune&limit=2"
            + "&fields=key,title,author_name,first_publish_year,cover_i";

    @Autowired
    private OpenLibraryClient client;

    @Autowired
    private MockRestServiceServer server;

    @DisplayName("должен запрашивать поиск и разбирать ответ")
    @Test
    void shouldSearchAndParseResponse() {
        server.expect(requestTo(SEARCH_URL))
                .andExpect(method(GET))
                .andExpect(header(HttpHeaders.USER_AGENT, "otus-spring-hw18-test"))
                .andRespond(withSuccess("""
                        {"numFound": 42, "docs": [
                          {"key": "/works/OL1W", "title": "Dune", "author_name": ["Frank Herbert"],
                           "first_publish_year": 1965, "cover_i": 123, "unknown_field": true},
                          {"key": "/works/OL2W", "title": "No authors"}
                        ]}
                        """, MediaType.APPLICATION_JSON));

        var response = client.search("dune", 2);

        server.verify();
        assertThat(response.numFound()).isEqualTo(42);
        assertThat(response.docs()).containsExactly(
                new OpenLibraryDoc("/works/OL1W", "Dune", List.of("Frank Herbert"), 1965, 123L),
                new OpenLibraryDoc("/works/OL2W", "No authors", null, null, null));
    }

    @DisplayName("должен пробрасывать ошибку HTTP-клиента при ошибке сервиса")
    @Test
    void shouldPropagateErrorWhenServiceFails() {
        server.expect(requestTo(SEARCH_URL)).andRespond(withServerError());

        assertThatThrownBy(() -> client.search("dune", 2))
                .isInstanceOf(HttpServerErrorException.class);
    }
}
