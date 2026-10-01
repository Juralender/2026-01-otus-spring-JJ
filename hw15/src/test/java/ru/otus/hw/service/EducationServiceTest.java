package ru.otus.hw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.hw.domain.Education;
import ru.otus.hw.domain.Gender;
import ru.otus.hw.domain.Person;
import ru.otus.hw.domain.Trait;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EducationServiceTest {

    private RandomEvents randomEvents;

    private EducationService educationService;

    private Person person;

    @BeforeEach
    void setUp() {
        randomEvents = mock(RandomEvents.class);
        educationService = new EducationService(randomEvents, TestMessages.english());
        person = new Person("Nora", "Wright", Gender.FEMALE, 1, null);
    }

    @DisplayName("умный ребёнок имеет повышенный шанс поступить в университет")
    @Test
    void smartChildShouldHaveBetterUniversityChance() {
        person.setTrait(Trait.SMART);
        when(randomEvents.chance(0.5)).thenReturn(true);

        educationService.enroll(person);

        assertThat(person.getEducation()).isEqualTo(Education.UNIVERSITY);
        verify(randomEvents).chance(0.5);
    }

    @DisplayName("без удачи ребёнок заканчивает только школу")
    @Test
    void unluckyChildShouldFinishOnlySchool() {
        person.setTrait(Trait.STRONG);
        when(randomEvents.chance(anyDouble())).thenReturn(false);

        educationService.enroll(person);

        assertThat(person.getEducation()).isEqualTo(Education.SCHOOL);
    }

    @DisplayName("отчисление из колледжа понижает образование до школьного")
    @Test
    void collegeDropoutShouldFallBackToSchool() {
        person.setEducation(Education.COLLEGE);
        when(randomEvents.chance(EducationService.DROPOUT_CHANCE)).thenReturn(true);

        educationService.studyAtCollege(person);

        assertThat(person.getEducation()).isEqualTo(Education.SCHOOL);
    }

    @DisplayName("отчисление из университета понижает образование до колледжа")
    @Test
    void universityDropoutShouldFallBackToCollege() {
        person.setEducation(Education.UNIVERSITY);
        when(randomEvents.chance(EducationService.DROPOUT_CHANCE)).thenReturn(true);

        educationService.studyAtUniversity(person);

        assertThat(person.getEducation()).isEqualTo(Education.COLLEGE);
    }

    @DisplayName("успешный выпускник университета сохраняет высшее образование")
    @Test
    void universityGraduateShouldKeepEducation() {
        person.setEducation(Education.UNIVERSITY);
        when(randomEvents.chance(EducationService.DROPOUT_CHANCE)).thenReturn(false);

        educationService.studyAtUniversity(person);

        assertThat(person.getEducation()).isEqualTo(Education.UNIVERSITY);
    }
}
