package ru.otus.hw.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.hw.config.LifeProperties;
import ru.otus.hw.domain.BirthRequest;
import ru.otus.hw.domain.FamilyPlan;
import ru.otus.hw.domain.Gender;
import ru.otus.hw.domain.Person;
import ru.otus.hw.domain.Stage;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FamilyServiceTest {

    private final RandomEvents randomEvents = mock(RandomEvents.class);

    private final FamilyService familyService = new FamilyService(randomEvents,
            new LifeProperties(1, 2, null, Locale.US), TestMessages.english());

    @DisplayName("на последнем поколении детей не бывает")
    @Test
    void lastGenerationShouldHaveNoChildren() {
        Person person = new Person("Arthur", "Maxson", Gender.MALE, 2, null);

        FamilyPlan plan = familyService.planChildren(person);

        assertThat(plan.hasChildren()).isFalse();
        verifyNoInteractions(randomEvents);
    }

    @DisplayName("дети рождаются в следующем поколении с фамилией родителя")
    @Test
    void childrenShouldBeConceivedForNextGeneration() {
        Person person = new Person("Arthur", "Maxson", Gender.MALE, 1, null);
        when(randomEvents.between(anyInt(), anyInt())).thenReturn(2);

        List<BirthRequest> requests = familyService.conceive(familyService.planChildren(person));

        assertThat(requests).hasSize(2)
                .allSatisfy(request -> {
                    assertThat(request.familyName()).isEqualTo("Maxson");
                    assertThat(request.generation()).isEqualTo(2);
                    assertThat(request.parent()).isSameAs(person);
                });
    }

    @DisplayName("выросшие дети добавляются к родителю")
    @Test
    void raisedChildrenShouldBeAttachedToParent() {
        Person parent = new Person("Arthur", "Maxson", Gender.MALE, 1, null);
        parent.setPartner(new Person("Sarah", "Lyons", Gender.FEMALE, 1, null));
        Person child = new Person("Moira", "Maxson", Gender.FEMALE, 2, parent);

        Person result = familyService.raise(List.of(child));

        assertThat(result).isSameAs(parent);
        assertThat(parent.getChildren()).containsExactly(child);
        assertThat(parent.getLastEvent().stage()).isEqualTo(Stage.FAMILY);
        assertThat(parent.getLastEvent().description()).contains("1 child with Sarah");
    }
}
