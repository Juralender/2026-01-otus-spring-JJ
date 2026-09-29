package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.config.LifeProperties;
import ru.otus.hw.domain.BirthRequest;
import ru.otus.hw.domain.FamilyPlan;
import ru.otus.hw.domain.Person;
import ru.otus.hw.domain.Stage;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class FamilyService {

    static final int MAX_CHILDREN = 3;

    private final RandomEvents randomEvents;

    private final LifeProperties properties;

    private final LocalizedMessagesService messages;

    public FamilyPlan planChildren(Person person) {
        int children = person.getGeneration() < properties.maxGenerations()
                ? randomEvents.between(0, MAX_CHILDREN)
                : 0;
        return new FamilyPlan(person, children);
    }

    public List<BirthRequest> conceive(FamilyPlan plan) {
        Person parent = plan.parent();
        return IntStream.range(0, plan.children())
                .mapToObj(i -> new BirthRequest(parent.getFamilyName(), parent.getGeneration() + 1, parent))
                .toList();
    }

    public Person raise(List<?> children) {
        List<Person> raised = children.stream().map(Person.class::cast).toList();
        Person parent = raised.get(0).getParent();
        parent.getChildren().addAll(raised);
        return parent.addEvent(Stage.FAMILY, messages.getMessage("event.family.children", raised.size(),
                parent.getPartner().getFirstName(),
                String.join(", ", raised.stream().map(Person::getFirstName).toList())));
    }

    public Person stayChildless(FamilyPlan plan) {
        Person parent = plan.parent();
        return parent.addEvent(Stage.FAMILY,
                messages.getMessage("event.family.childless", parent.getPartner().getFirstName()));
    }

    public Person liveAlone(Person person) {
        return person.addEvent(Stage.FAMILY, messages.getMessage("event.family.alone"));
    }
}
