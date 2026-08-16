package ru.otus.hw.controllers.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.otus.hw.services.dto.GenreCreateDto;
import ru.otus.hw.services.dto.GenreDto;
import ru.otus.hw.services.dto.GenreUpdateDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenreFormDto {

    private long id;

    @NotBlank(message = "Name must not be empty")
    @Size(max = 10, message = "Name must not exceed 10 characters")
    private String name;

    public static GenreFormDto fromDto(GenreDto genre) {
        return new GenreFormDto(genre.getId(), genre.getName());
    }

    public GenreCreateDto toCreateDto() {
        return new GenreCreateDto(name);
    }

    public GenreUpdateDto toUpdateDto(long id) {
        return new GenreUpdateDto(id, name);
    }
}
