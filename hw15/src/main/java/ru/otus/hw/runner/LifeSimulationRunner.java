package ru.otus.hw.runner;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.otus.hw.config.LifeProperties;
import ru.otus.hw.domain.BirthRequest;
import ru.otus.hw.domain.Person;
import ru.otus.hw.gateway.LifeGateway;
import ru.otus.hw.service.BiographyPrinter;
import ru.otus.hw.service.NameGenerator;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "life.simulation.enabled", havingValue = "true", matchIfMissing = true)
public class LifeSimulationRunner implements CommandLineRunner {

    private final LifeGateway lifeGateway;

    private final NameGenerator nameGenerator;

    private final BiographyPrinter biographyPrinter;

    private final LifeProperties properties;

    @Override
    public void run(String... args) {
        for (int i = 0; i < properties.people(); i++) {
            Person person = lifeGateway.live(BirthRequest.firstGeneration(nameGenerator.familyName()));
            System.out.println();
            System.out.println(biographyPrinter.print(person));
        }
    }
}
