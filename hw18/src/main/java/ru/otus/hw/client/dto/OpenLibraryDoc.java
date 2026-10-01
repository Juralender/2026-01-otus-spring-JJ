package ru.otus.hw.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenLibraryDoc(
        String key,
        String title,
        @JsonProperty("author_name") List<String> authorNames,
        @JsonProperty("first_publish_year") Integer firstPublishYear,
        @JsonProperty("cover_i") Long coverId) {
}
