package ru.otus.hw.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.hw.domain.Gender;
import ru.otus.hw.domain.Person;
import ru.otus.hw.domain.Profession;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoveServiceTest {

    @DisplayName("без удачи человек остаётся одиноким, а шанс растёт с числом друзей")
    @Test
    void shouldStaySingleWhenUnlucky() {
        RandomEvents randomEvents = mock(RandomEvents.class);
        LoveService loveService = new LoveService(randomEvents,
                new NameGenerator(randomEvents, TestMessages.english()), TestMessages.english());
        Person person = new Person("Boone", "Stone", Gender.MALE, 1, null);
        person.getFriends().addAll(List.of("A", "B"));
        when(randomEvents.chance(anyDouble())).thenReturn(false);

        loveService.seekLove(person);

        assertThat(person.isMarried()).isFalse();
        verify(randomEvents).chance(0.5);
    }

    @DisplayName("супруг противоположного пола и того же поколения")
    @Test
    void partnerShouldBeOfOppositeGender() {
        RandomEvents randomEvents = new RandomEvents(new Random(3));
        LoveService loveService = new LoveService(randomEvents,
                new NameGenerator(randomEvents, TestMessages.english()), TestMessages.english());
        for (int i = 0; i < 100; i++) {
            Person person = new Person("Cass", "Lyons", Gender.FEMALE, 2, null);
            loveService.seekLove(person);
            if (person.isMarried()) {
                assertThat(person.getPartner().getGender()).isEqualTo(Gender.MALE);
                assertThat(person.getPartner().getGeneration()).isEqualTo(2);
                assertThat(person.getPartner().getProfession()).isIn((Object[]) Profession.values());
            }
        }
    }
}
