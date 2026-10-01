package ru.otus.hw.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Locale;

@ConfigurationProperties(prefix = "life")
public record LifeProperties(int people, int maxGenerations, Long seed, Locale locale) implements LocaleConfig {

    @Override
    public Locale getLocale() {
        return locale;
    }
}
