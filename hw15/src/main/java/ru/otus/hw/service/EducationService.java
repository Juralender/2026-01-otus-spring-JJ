package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.domain.Education;
import ru.otus.hw.domain.Person;
import ru.otus.hw.domain.Stage;
import ru.otus.hw.domain.Trait;

@Service
@RequiredArgsConstructor
public class EducationService {

    static final double DROPOUT_CHANCE = 0.2;

    private final RandomEvents randomEvents;

    private final LocalizedMessagesService messages;

    public Person enroll(Person person) {
        double universityChance = person.getTrait() == Trait.SMART ? 0.5 : 0.15;
        if (randomEvents.chance(universityChance)) {
            person.setEducation(Education.UNIVERSITY);
        } else if (randomEvents.chance(0.5)) {
            person.setEducation(Education.COLLEGE);
        } else {
            person.setEducation(Education.SCHOOL);
        }
        return person;
    }

    public Person finishSchool(Person person) {
        return person.addEvent(Stage.EDUCATION, messages.getMessage("event.education.school"));
    }

    public Person studyAtCollege(Person person) {
        if (randomEvents.chance(DROPOUT_CHANCE)) {
            person.setEducation(Education.SCHOOL);
            return person.addEvent(Stage.EDUCATION, messages.getMessage("event.education.college.dropout"));
        }
        return person.addEvent(Stage.EDUCATION, messages.getMessage("event.education.college.graduated"));
    }

    public Person studyAtUniversity(Person person) {
        if (randomEvents.chance(DROPOUT_CHANCE)) {
            person.setEducation(Education.COLLEGE);
            return person.addEvent(Stage.EDUCATION, messages.getMessage("event.education.university.dropout"));
        }
        return person.addEvent(Stage.EDUCATION, messages.getMessage("event.education.university.graduated"));
    }
}
