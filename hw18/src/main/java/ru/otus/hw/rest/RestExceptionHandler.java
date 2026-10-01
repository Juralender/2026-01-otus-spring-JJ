package ru.otus.hw.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.otus.hw.exceptions.ExternalServiceException;

@Slf4j
@RestControllerAdvice(basePackages = "ru.otus.hw.rest.controllers")
public class RestExceptionHandler {

    @ExceptionHandler(ExternalServiceException.class)
    public ProblemDetail handleExternalService(ExternalServiceException ex) {
        log.warn("External service call failed", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    @ExceptionHandler({
            HandlerMethodValidationException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ProblemDetail handleBadRequest(Exception ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request parameters");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }
}
