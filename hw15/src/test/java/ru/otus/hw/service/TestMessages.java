package ru.otus.hw.service;

import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

final class TestMessages {

    private TestMessages() {
    }

    static LocalizedMessagesService english() {
        return forLocale(Locale.US);
    }

    static LocalizedMessagesService forLocale(Locale locale) {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        return new LocalizedMessagesServiceImpl(messageSource, () -> locale);
    }
}
