package ru.otus.hw.flow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.otus.hw.config.LifeProperties;
import ru.otus.hw.domain.BirthRequest;
import ru.otus.hw.domain.LifeEvent;
import ru.otus.hw.domain.Person;
import ru.otus.hw.domain.Stage;
import ru.otus.hw.gateway.LifeGateway;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LifeFlowTest {

    private static final int LIVES = 50;

    @Autowired
    private LifeGateway lifeGateway;

    @Autowired
    private LifeProperties properties;

    @DisplayName("гейтвей проводит человека через все этапы жизни")
    @Test
    void shouldLiveThroughAllStages() {
        Person person = lifeGateway.live(BirthRequest.firstGeneration("Maxson"));

        assertThat(person.getFamilyName()).isEqualTo("Maxson");
        assertThat(person.getGeneration()).isEqualTo(1);
        assertThat(person.getBiography()).extracting(LifeEvent::stage).containsExactly(Stage.values());
    }

    @DisplayName("случайные события каждого этапа согласованы с предыдущими во всех поколениях")
    @Test
    void randomEventsShouldRespectPreviousChoices() {
        List<Person> everyone = new ArrayList<>();
        for (int i = 0; i < LIVES; i++) {
            collect(lifeGateway.live(BirthRequest.firstGeneration("Stone")), everyone);
        }

        assertThat(everyone).hasSizeGreaterThan(LIVES).allSatisfy(person -> {
            assertThat(person.getBiography()).extracting(LifeEvent::stage).containsExactly(Stage.values());
            assertThat(person.getEducation().isAtLeast(person.getProfession().getRequiredEducation())).isTrue();
            assertThat(person.getGeneration()).isBetween(1, properties.maxGenerations());
            if (!person.isMarried()) {
                assertThat(person.getChildren()).isEmpty();
            }
            person.getChildren().forEach(child -> {
                assertThat(child.getParent()).isSameAs(person);
                assertThat(child.getGeneration()).isEqualTo(person.getGeneration() + 1);
                assertThat(child.getFamilyName()).isEqualTo(person.getFamilyName());
            });
        });
        assertThat(everyone).anyMatch(person -> person.getGeneration() == properties.maxGenerations());
    }

    private void collect(Person person, List<Person> everyone) {
        everyone.add(person);
        person.getChildren().forEach(child -> collect(child, everyone));
    }
}
