package ru.otus.hw.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "open-library")
public record OpenLibraryProperties(String baseUrl, String coversUrl, String userAgent) {
}
