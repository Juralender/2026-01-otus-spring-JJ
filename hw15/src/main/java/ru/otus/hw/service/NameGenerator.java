package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.domain.Gender;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NameGenerator {

    private final RandomEvents randomEvents;

    private final LocalizedMessagesService messages;

    public String firstName(Gender gender) {
        return randomEvents.pick(names("names.first." + gender.name()));
    }

    public String familyName() {
        return randomEvents.pick(names("names.family"));
    }

    public String fullName() {
        return firstName(randomEvents.pick(List.of(Gender.values()))) + " " + familyName();
    }

    private List<String> names(String code) {
        return Arrays.stream(messages.getMessage(code).split(",")).map(String::trim).toList();
    }
}
