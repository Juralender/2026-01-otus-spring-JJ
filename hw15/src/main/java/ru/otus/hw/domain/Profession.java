package ru.otus.hw.domain;

import java.util.Arrays;
import java.util.List;

public enum Profession {
    WORKER(Education.SCHOOL),
    DRIVER(Education.SCHOOL),
    COOK(Education.SCHOOL),
    SOLDIER(Education.SCHOOL),
    ENGINEER(Education.COLLEGE),
    TEACHER(Education.COLLEGE),
    NURSE(Education.COLLEGE),
    SCIENTIST(Education.UNIVERSITY),
    DOCTOR(Education.UNIVERSITY),
    LAWYER(Education.UNIVERSITY);

    private final Education requiredEducation;

    Profession(Education requiredEducation) {
        this.requiredEducation = requiredEducation;
    }

    public Education getRequiredEducation() {
        return requiredEducation;
    }

    public static List<Profession> availableFor(Education education) {
        return Arrays.stream(values())
                .filter(profession -> education.isAtLeast(profession.requiredEducation))
                .toList();
    }
}
