package ru.otus.hw.gateway;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import ru.otus.hw.config.Channels;
import ru.otus.hw.domain.BirthRequest;
import ru.otus.hw.domain.Person;

@MessagingGateway
public interface LifeGateway {

    @Gateway(requestChannel = Channels.LIFE)
    Person live(BirthRequest request);
}
