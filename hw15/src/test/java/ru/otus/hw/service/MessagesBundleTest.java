package ru.otus.hw.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.hw.domain.ChildhoodEvent;
import ru.otus.hw.domain.Gender;
import ru.otus.hw.domain.Profession;
import ru.otus.hw.domain.Stage;
import ru.otus.hw.domain.Trait;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class MessagesBundleTest {

    @DisplayName("русский и английский бандлы содержат одинаковый набор ключей")
    @Test
    void bundlesShouldHaveSameKeys() throws IOException {
        assertThat(load("messages_ru_RU.properties").stringPropertyNames())
                .containsExactlyInAnyOrderElementsOf(load("messages.properties").stringPropertyNames());
    }

    @DisplayName("для каждого значения перечислений есть перевод на обоих языках")
    @Test
    void everyEnumValueShouldBeLocalized() {
        Stream<String> codes = Stream.of(
                keys("gender.", Gender.values()), keys("spouse.", Gender.values()),
                keys("names.first.", Gender.values()), keys("trait.", Trait.values()),
                keys("stage.", Stage.values()), keys("profession.", Profession.values()),
                keys("childhood.", ChildhoodEvent.values())).flatMap(s -> s);
        LocalizedMessagesService english = TestMessages.english();
        LocalizedMessagesService russian = TestMessages.forLocale(Locale.forLanguageTag("ru-RU"));

        assertThat(codes).allSatisfy(code -> {
            assertThat(english.getMessage(code)).isNotBlank();
            assertThat(russian.getMessage(code)).isNotBlank().isNotEqualTo(english.getMessage(code));
        });
    }

    @DisplayName("русская локаль выдаёт русские тексты событий")
    @Test
    void russianLocaleShouldProduceRussianEvents() {
        LocalizedMessagesService russian = TestMessages.forLocale(Locale.forLanguageTag("ru-RU"));

        assertThat(russian.getMessage("event.career", russian.getMessage("profession.SCIENTIST")))
                .isEqualTo("Начало карьеры: учёный");
    }

    private static Stream<String> keys(String prefix, Enum<?>[] values) {
        return Arrays.stream(values).map(value -> prefix + value.name());
    }

    private static Properties load(String name) throws IOException {
        Properties properties = new Properties();
        try (var reader = new InputStreamReader(
                MessagesBundleTest.class.getClassLoader().getResourceAsStream(name), StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return properties;
    }
}
