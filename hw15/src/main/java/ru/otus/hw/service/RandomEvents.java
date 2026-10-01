package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class RandomEvents {

    private final Random random;

    public boolean chance(double probability) {
        return random.nextDouble() < probability;
    }

    public int between(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    public <T> T pick(List<T> options) {
        return options.get(random.nextInt(options.size()));
    }
}
