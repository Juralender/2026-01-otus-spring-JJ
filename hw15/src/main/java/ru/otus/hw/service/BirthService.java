package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.domain.BirthRequest;
import ru.otus.hw.domain.Gender;
import ru.otus.hw.domain.Person;
import ru.otus.hw.domain.Stage;
import ru.otus.hw.domain.Trait;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BirthService {

    private final RandomEvents randomEvents;

    private final NameGenerator nameGenerator;

    private final LocalizedMessagesService messages;

    public Person bear(BirthRequest request) {
        Gender gender = randomEvents.pick(List.of(Gender.values()));
        Person person = new Person(nameGenerator.firstName(gender), request.familyName(), gender,
                request.generation(), request.parent());
        person.setTrait(randomEvents.pick(List.of(Trait.values())));
        String origin = request.parent() == null
                ? messages.getMessage("event.birth.origin.vault")
                : messages.getMessage("event.birth.origin.parent", request.parent().getFullName());
        return person.addEvent(Stage.BIRTH, messages.getMessage("event.birth",
                messages.getMessage("gender." + gender.name()), origin, person.getFirstName(),
                messages.getMessage("trait." + person.getTrait().name())));
    }
}
