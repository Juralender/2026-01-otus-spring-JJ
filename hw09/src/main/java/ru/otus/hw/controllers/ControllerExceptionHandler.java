package ru.otus.hw.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import ru.otus.hw.exceptions.EntityNotFoundException;

@Slf4j
@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleEntityNotFound(EntityNotFoundException ex) {
        return new ModelAndView("error")
                .addObject("message", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleIllegalArgument(IllegalArgumentException ex) {
        return new ModelAndView("error")
                .addObject("message", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception ex) {
        if (ex instanceof ErrorResponse errorResponse) {
            var status = HttpStatus.valueOf(errorResponse.getStatusCode().value());
            return new ModelAndView("error", status)
                    .addObject("message", ex.getMessage());
        }

        log.error("Unhandled exception", ex);
        return new ModelAndView("error", HttpStatus.INTERNAL_SERVER_ERROR)
                .addObject("message", "Internal server error");
    }
}
