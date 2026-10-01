package ru.otus.hw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
public class RandomConfig {

    @Bean
    public Random random(LifeProperties properties) {
        return properties.seed() == null ? new Random() : new Random(properties.seed());
    }
}
