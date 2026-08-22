package ru.otus.hw.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.otus.hw.exceptions.EntityNotFoundException;

import java.util.stream.Collectors;

@Slf4j
@Component
@Order(-2)
@RequiredArgsConstructor
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        var problemDetail = toProblemDetail(ex);
        var response = exchange.getResponse();
        response.setStatusCode(HttpStatusCode.valueOf(problemDetail.getStatus()));
        response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);

        byte[] body;
        try {
            body = objectMapper.writeValueAsBytes(problemDetail);
        } catch (JsonProcessingException e) {
            body = new byte[0];
        }
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
    }

    private ProblemDetail toProblemDetail(Throwable ex) {
        if (ex instanceof EntityNotFoundException e) {
            return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        }
        if (ex instanceof IllegalArgumentException e) {
            return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        if (ex instanceof WebExchangeBindException e) {
            return validationProblemDetail(e.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a)));
        }
        if (ex instanceof ConstraintViolationException e) {
            return validationProblemDetail(e.getConstraintViolations().stream()
                    .collect(Collectors.toMap(
                            v -> v.getPropertyPath().toString(),
                            ConstraintViolation::getMessage,
                            (a, b) -> a)));
        }
        log.error("Unhandled exception", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    private ProblemDetail validationProblemDetail(Object errors) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problemDetail.setProperty("errors", errors);
        return problemDetail;
    }
}
