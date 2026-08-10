package ru.otus.hw.controllers.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.otus.hw.models.Genre;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenreFormDto {

    private long id;

    @NotBlank(message = "Name must not be empty")
    @Size(max = 10, message = "Name must not exceed 10 characters")
    private String name;

    public static GenreFormDto fromGenre(Genre genre) {
        return new GenreFormDto(genre.getId(), genre.getName());
    }
}
