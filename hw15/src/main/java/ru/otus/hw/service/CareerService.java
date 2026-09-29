package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.domain.Person;
import ru.otus.hw.domain.Profession;
import ru.otus.hw.domain.Stage;

@Service
@RequiredArgsConstructor
public class CareerService {

    private final RandomEvents randomEvents;

    private final LocalizedMessagesService messages;

    public Person startWorking(Person person) {
        Profession profession = randomEvents.pick(Profession.availableFor(person.getEducation()));
        person.setProfession(profession);
        return person.addEvent(Stage.CAREER,
                messages.getMessage("event.career", messages.getMessage("profession." + profession.name())));
    }
}
