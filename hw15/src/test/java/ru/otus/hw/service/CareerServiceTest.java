package ru.otus.hw.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ru.otus.hw.domain.Education;
import ru.otus.hw.domain.Gender;
import ru.otus.hw.domain.Person;
import ru.otus.hw.domain.Profession;
import ru.otus.hw.domain.Stage;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CareerServiceTest {

    private final CareerService careerService = new CareerService(new RandomEvents(new Random(1)),
            TestMessages.english());

    @DisplayName("выбирает профессию только из доступных для уровня образования")
    @ParameterizedTest
    @EnumSource(Education.class)
    void shouldChooseOnlyProfessionsAllowedByEducation(Education education) {
        Set<Profession> chosen = EnumSet.noneOf(Profession.class);
        for (int i = 0; i < 500; i++) {
            Person person = new Person("Nate", "Stone", Gender.MALE, 1, null);
            person.setEducation(education);
            careerService.startWorking(person);
            chosen.add(person.getProfession());
            assertThat(person.getLastEvent().stage()).isEqualTo(Stage.CAREER);
        }
        assertThat(chosen).containsExactlyInAnyOrderElementsOf(Profession.availableFor(education));
    }

    @DisplayName("выпускник школы никогда не становится учёным")
    @Test
    void schoolGraduateShouldNeverBecomeScientist() {
        assertThat(Profession.availableFor(Education.SCHOOL))
                .containsExactlyInAnyOrder(Profession.WORKER, Profession.DRIVER, Profession.COOK, Profession.SOLDIER)
                .doesNotContain(Profession.SCIENTIST, Profession.DOCTOR, Profession.ENGINEER);
        assertThat(Profession.availableFor(Education.COLLEGE)).doesNotContain(Profession.SCIENTIST);
        assertThat(Profession.availableFor(Education.UNIVERSITY)).contains(Profession.SCIENTIST);
    }
}
