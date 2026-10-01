package ru.otus.hw.services;

import java.nio.file.Path;

public interface SqlDataFileService {

    void save(Path file);

    void restore(Path file);
}
