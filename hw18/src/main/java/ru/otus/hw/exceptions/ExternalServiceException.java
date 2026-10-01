package ru.otus.hw.exceptions;

public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
