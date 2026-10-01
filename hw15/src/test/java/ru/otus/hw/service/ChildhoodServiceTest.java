package ru.otus.hw.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.hw.domain.ChildhoodEvent;
import ru.otus.hw.domain.Gender;
import ru.otus.hw.domain.Person;
import ru.otus.hw.domain.Stage;
import ru.otus.hw.domain.Trait;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChildhoodServiceTest {

    private final RandomEvents randomEvents = mock(RandomEvents.class);

    private final ChildhoodService childhoodService = new ChildhoodService(randomEvents, TestMessages.english());

    @DisplayName("событие детства меняет черту характера")
    @Test
    void childhoodEventShouldChangeTrait() {
        Person person = new Person("Butch", "Burke", Gender.MALE, 1, null);
        person.setTrait(Trait.STRONG);
        when(randomEvents.pick(anyList())).thenReturn(ChildhoodEvent.LIBRARY_CARD);

        childhoodService.grow(person);

        assertThat(person.getTrait()).isEqualTo(Trait.SMART);
        assertThat(person.getLastEvent().stage()).isEqualTo(Stage.CHILDHOOD);
        assertThat(person.getLastEvent().description()).contains("became smart");
    }

    @DisplayName("спокойное детство не меняет черту характера")
    @Test
    void quietChildhoodShouldKeepTrait() {
        Person person = new Person("Butch", "Burke", Gender.MALE, 1, null);
        person.setTrait(Trait.CHARISMATIC);
        when(randomEvents.pick(anyList())).thenReturn(ChildhoodEvent.QUIET_CHILDHOOD);

        childhoodService.grow(person);

        assertThat(person.getTrait()).isEqualTo(Trait.CHARISMATIC);
    }
}
