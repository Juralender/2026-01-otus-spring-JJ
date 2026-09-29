package ru.otus.hw.domain;

public enum Education {
    SCHOOL,
    COLLEGE,
    UNIVERSITY;

    public boolean isAtLeast(Education required) {
        return compareTo(required) >= 0;
    }
}
