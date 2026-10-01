package ru.otus.hw.domain;

public record FamilyPlan(Person parent, int children) {

    public boolean hasChildren() {
        return children > 0;
    }
}
