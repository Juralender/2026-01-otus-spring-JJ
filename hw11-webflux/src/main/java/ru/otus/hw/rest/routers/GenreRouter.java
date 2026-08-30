package ru.otus.hw.rest.routers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import ru.otus.hw.rest.handlers.GenreHandler;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

@Configuration
public class GenreRouter {

    @Bean
    public RouterFunction<ServerResponse> genreRoutes(GenreHandler handler) {
        return RouterFunctions.route()
                .GET("/api/genres", accept(MediaType.APPLICATION_JSON), handler::findAll)
                .GET("/api/genres/{id}", accept(MediaType.APPLICATION_JSON), handler::findById)
                .POST("/api/genres", contentType(MediaType.APPLICATION_JSON), handler::create)
                .PUT("/api/genres/{id}", contentType(MediaType.APPLICATION_JSON), handler::update)
                .build();
    }
}
