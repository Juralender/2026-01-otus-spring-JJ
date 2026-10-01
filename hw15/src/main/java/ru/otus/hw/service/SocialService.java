package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.domain.Acquaintance;
import ru.otus.hw.domain.Person;
import ru.otus.hw.domain.Stage;
import ru.otus.hw.domain.Trait;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class SocialService {

    static final int MAX_ACQUAINTANCES = 5;

    private final RandomEvents randomEvents;

    private final NameGenerator nameGenerator;

    private final LocalizedMessagesService messages;

    public List<Acquaintance> meetPeople(Person person) {
        return IntStream.range(0, randomEvents.between(1, MAX_ACQUAINTANCES))
                .mapToObj(i -> new Acquaintance(person, nameGenerator.fullName(), false))
                .toList();
    }

    public Acquaintance getAcquainted(Acquaintance acquaintance) {
        double friendshipChance = acquaintance.owner().getTrait() == Trait.CHARISMATIC ? 0.8 : 0.4;
        return new Acquaintance(acquaintance.owner(), acquaintance.name(),
                randomEvents.chance(friendshipChance));
    }

    public Person makeFriends(List<?> acquaintances) {
        List<Acquaintance> met = acquaintances.stream().map(Acquaintance.class::cast).toList();
        Person person = met.get(0).owner();
        met.stream().filter(Acquaintance::friend).map(Acquaintance::name).forEach(person.getFriends()::add);
        String description = person.getFriends().isEmpty()
                ? messages.getMessage("event.friends.none", met.size())
                : messages.getMessage("event.friends.made", met.size(), String.join(", ", person.getFriends()));
        return person.addEvent(Stage.FRIENDS, description);
    }
}
