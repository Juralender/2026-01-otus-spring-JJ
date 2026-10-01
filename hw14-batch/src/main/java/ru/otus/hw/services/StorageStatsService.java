package ru.otus.hw.services;

import java.util.Map;

public interface StorageStatsService {

    Map<String, Long> relationalCounts();

    Map<String, Long> mongoCounts();
}
