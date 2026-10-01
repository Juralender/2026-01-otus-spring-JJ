package ru.otus.hw.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Person {

    private final String firstName;

    private final String familyName;

    private final Gender gender;

    private final int generation;

    private final Person parent;

    private Trait trait;

    private Education education;

    private Profession profession;

    private Person partner;

    private final List<String> friends = new ArrayList<>();

    private final List<Person> children = new ArrayList<>();

    private final List<LifeEvent> biography = new ArrayList<>();

    public Person(String firstName, String familyName, Gender gender, int generation, Person parent) {
        this.firstName = firstName;
        this.familyName = familyName;
        this.gender = gender;
        this.generation = generation;
        this.parent = parent;
    }

    public String getFullName() {
        return firstName + " " + familyName;
    }

    public boolean isMarried() {
        return partner != null;
    }

    public Person addEvent(Stage stage, String description) {
        biography.add(new LifeEvent(stage, description));
        return this;
    }

    public LifeEvent getLastEvent() {
        return biography.isEmpty() ? null : biography.get(biography.size() - 1);
    }

    @Override
    public String toString() {
        return "Person{" + getFullName() + ", generation=" + generation + "}";
    }
}
