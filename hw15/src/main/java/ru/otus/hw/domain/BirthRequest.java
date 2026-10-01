package ru.otus.hw.domain;

public record BirthRequest(String familyName, int generation, Person parent) {

    public static BirthRequest firstGeneration(String familyName) {
        return new BirthRequest(familyName, 1, null);
    }
}
