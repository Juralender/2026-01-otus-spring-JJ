package ru.otus.hw.domain;

public enum ChildhoodEvent {
    LIBRARY_CARD(Trait.SMART),
    YARD_FIGHTS(Trait.STRONG),
    SCHOOL_THEATER(Trait.CHARISMATIC),
    RADROACH_HUNT(Trait.STRONG),
    QUIET_CHILDHOOD(null);

    private final Trait developedTrait;

    ChildhoodEvent(Trait developedTrait) {
        this.developedTrait = developedTrait;
    }

    public Trait getDevelopedTrait() {
        return developedTrait;
    }
}
