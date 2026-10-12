package ru.otus.hw.client;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import ru.otus.hw.exceptions.ExternalServiceException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Отказоустойчивость клиента Open Library")
@SpringBootTest(properties = {
        "resilience4j.retry.instances.openLibrary.wait-duration=10ms",
        "resilience4j.circuitbreaker.instances.openLibrary.wait-duration-in-open-state=1h"
})
@AutoConfigureMockRestServiceServer
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenLibraryClientResilienceTest {

    private static final String SEARCH_URL = "https://openlibrary.test/search.json";

    private static final String SEARCH_RESPONSE = """
            {"numFound": 1, "docs": [{"key": "/works/OL1W", "title": "Dune"}]}
            """;

    @Autowired
    private OpenLibraryClient client;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private MockMvc mvc;

    private CircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp() {
        server.reset();
        circuitBreaker = circuitBreakerRegistry.circuitBreaker("openLibrary");
        circuitBreaker.reset();
    }

    @DisplayName("должен повторять запрос при ошибке сервера и вернуть успешный ответ")
    @Test
    void shouldRetryOnServerErrorAndSucceed() {
        server.expect(times(2), requestTo(startsWith(SEARCH_URL))).andRespond(withServerError());
        server.expect(once(), requestTo(startsWith(SEARCH_URL)))
                .andRespond(withSuccess(SEARCH_RESPONSE, MediaType.APPLICATION_JSON));

        var response = client.search("dune", 1);

        server.verify();
        assertThat(response.numFound()).isEqualTo(1);
        assertThat(response.docs()).hasSize(1);
    }

    @DisplayName("должен вызывать fallback после исчерпания всех попыток")
    @Test
    void shouldFallbackWhenRetriesExhausted() {
        server.expect(times(3), requestTo(startsWith(SEARCH_URL))).andRespond(withServerError());

        assertThatThrownBy(() -> client.search("dune", 1))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessage("Open Library is unavailable")
                .hasCauseInstanceOf(HttpServerErrorException.class);
        server.verify();
    }

    @DisplayName("не должен повторять запрос и учитывать его в circuit breaker при ошибке клиента")
    @Test
    void shouldNotRetryOnClientError() {
        server.expect(once(), requestTo(startsWith(SEARCH_URL))).andRespond(withBadRequest());

        assertThatThrownBy(() -> client.search("dune", 1))
                .isInstanceOf(ExternalServiceException.class)
                .hasCauseInstanceOf(HttpClientErrorException.class);
        server.verify();
        assertThat(circuitBreaker.getMetrics().getNumberOfFailedCalls()).isZero();
    }

    @DisplayName("должен размыкать цепь после серии ошибок и не отправлять запросы")
    @Test
    void shouldOpenCircuitAfterFailures() {
        server.expect(times(5), requestTo(startsWith(SEARCH_URL))).andRespond(withServerError());

        assertThatThrownBy(() -> client.search("dune", 1)).isInstanceOf(ExternalServiceException.class);
        assertThatThrownBy(() -> client.search("dune", 1))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessage("Open Library is temporarily unavailable, try again later")
                .hasCauseInstanceOf(CallNotPermittedException.class);

        server.verify();
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @DisplayName("должен возвращать 502 из REST API, пока цепь разомкнута")
    @Test
    void shouldReturnBadGatewayWhenCircuitIsOpen() throws Exception {
        circuitBreaker.transitionToOpenState();

        mvc.perform(get("/api/books/search").param("q", "dune"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.detail").value("Open Library is temporarily unavailable, try again later"));

        server.verify();
    }
}
