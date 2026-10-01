package ru.otus.hw.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenLibrarySearchResponse(long numFound, List<OpenLibraryDoc> docs) {
}
