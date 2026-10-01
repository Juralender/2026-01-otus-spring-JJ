package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.domain.ChildhoodEvent;
import ru.otus.hw.domain.Person;
import ru.otus.hw.domain.Stage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChildhoodService {

    private final RandomEvents randomEvents;

    private final LocalizedMessagesService messages;

    public Person grow(Person person) {
        ChildhoodEvent event = randomEvents.pick(List.of(ChildhoodEvent.values()));
        String description = messages.getMessage("childhood." + event.name());
        if (event.getDevelopedTrait() != null && event.getDevelopedTrait() != person.getTrait()) {
            person.setTrait(event.getDevelopedTrait());
            description = messages.getMessage("event.childhood.trait-changed", description,
                    messages.getMessage("trait." + event.getDevelopedTrait().name()));
        }
        return person.addEvent(Stage.CHILDHOOD, description);
    }
}
