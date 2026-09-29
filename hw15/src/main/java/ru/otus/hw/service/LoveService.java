package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.domain.Gender;
import ru.otus.hw.domain.Person;
import ru.otus.hw.domain.Profession;
import ru.otus.hw.domain.Stage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoveService {

    private final RandomEvents randomEvents;

    private final NameGenerator nameGenerator;

    private final LocalizedMessagesService messages;

    public Person seekLove(Person person) {
        double loveChance = Math.min(0.9, 0.3 + 0.1 * person.getFriends().size());
        if (!randomEvents.chance(loveChance)) {
            return person.addEvent(Stage.LOVE, messages.getMessage("event.love.none"));
        }
        Gender gender = person.getGender().opposite();
        Person partner = new Person(nameGenerator.firstName(gender), nameGenerator.familyName(), gender,
                person.getGeneration(), null);
        partner.setProfession(randomEvents.pick(List.of(Profession.values())));
        person.setPartner(partner);
        return person.addEvent(Stage.LOVE, messages.getMessage("event.love.married", partner.getFullName(),
                messages.getMessage("profession." + partner.getProfession().name()),
                messages.getMessage("spouse." + gender.name())));
    }
}
