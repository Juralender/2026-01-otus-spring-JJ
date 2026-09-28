package ru.otus.hw.batch.mapping;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EntityKind {
    AUTHOR("author"),
    GENRE("genre"),
    BOOK("book"),
    COMMENT("comment");

    private final String storageName;
}
