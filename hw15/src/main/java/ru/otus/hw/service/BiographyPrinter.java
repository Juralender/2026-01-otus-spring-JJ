package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.domain.LifeEvent;
import ru.otus.hw.domain.Person;

@Component
@RequiredArgsConstructor
public class BiographyPrinter {

    private static final int STAGE_WIDTH = 12;

    private final LocalizedMessagesService messages;

    public String print(Person person) {
        StringBuilder builder = new StringBuilder();
        append(builder, person);
        return builder.toString();
    }

    private void append(StringBuilder builder, Person person) {
        String indent = "    ".repeat(person.getGeneration() - 1);
        builder.append(indent)
                .append(messages.getMessage("biography.header", person.getFullName(), person.getGeneration()))
                .append(System.lineSeparator());
        for (LifeEvent event : person.getBiography()) {
            String stage = messages.getMessage("stage." + event.stage().name());
            builder.append(indent).append("  ").append(String.format("%-" + STAGE_WIDTH + "s", stage))
                    .append("| ").append(event.description()).append(System.lineSeparator());
        }
        person.getChildren().forEach(child -> append(builder, child));
    }
}
