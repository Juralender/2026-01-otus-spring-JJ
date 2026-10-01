package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.otus.hw.domain.LifeEvent;
import ru.otus.hw.domain.Person;

@Slf4j
@Component
@RequiredArgsConstructor
public class StageLogger {

    private final LocalizedMessagesService messages;

    public void log(Person person) {
        LifeEvent event = person.getLastEvent();
        if (event != null) {
            log.info("{}[{}] {}: {}", "  ".repeat(person.getGeneration() - 1), person.getFullName(),
                    messages.getMessage("stage." + event.stage().name()), event.description());
        }
    }
}
