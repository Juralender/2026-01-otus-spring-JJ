package ru.otus.hw.domain;

public enum Gender {
    MALE,
    FEMALE;

    public Gender opposite() {
        return this == MALE ? FEMALE : MALE;
    }
}
