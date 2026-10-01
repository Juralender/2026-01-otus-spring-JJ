package ru.otus.hw.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.hw.domain.Acquaintance;
import ru.otus.hw.domain.Gender;
import ru.otus.hw.domain.Person;
import ru.otus.hw.domain.Stage;
import ru.otus.hw.domain.Trait;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SocialServiceTest {

    private final RandomEvents randomEvents = mock(RandomEvents.class);

    private final SocialService socialService = new SocialService(randomEvents,
            new NameGenerator(randomEvents, TestMessages.english()), TestMessages.english());

    @DisplayName("харизматичный человек легче заводит друзей")
    @Test
    void charismaticPersonShouldMakeFriendsEasier() {
        Person person = new Person("Piper", "Wright", Gender.FEMALE, 1, null);
        person.setTrait(Trait.CHARISMATIC);
        when(randomEvents.chance(0.8)).thenReturn(true);

        Acquaintance acquaintance = socialService.getAcquainted(new Acquaintance(person, "Nick Valentine", false));

        assertThat(acquaintance.friend()).isTrue();
    }

    @DisplayName("в друзья попадают только те знакомые, с кем сложилась дружба")
    @Test
    void shouldKeepOnlyFriends() {
        Person person = new Person("Piper", "Wright", Gender.FEMALE, 1, null);

        socialService.makeFriends(List.of(
                new Acquaintance(person, "Nick Valentine", true),
                new Acquaintance(person, "Preston Garvey", false),
                new Acquaintance(person, "Cait Stone", true)));

        assertThat(person.getFriends()).containsExactly("Nick Valentine", "Cait Stone");
        assertThat(person.getLastEvent().stage()).isEqualTo(Stage.FRIENDS);
        assertThat(person.getLastEvent().description()).startsWith("Met 3 people");
    }
}
