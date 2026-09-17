package ru.otus.hw.services.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenreCreateDto {

    @NotBlank(message = "Name must not be empty")
    @Size(max = 10, message = "Name must not exceed 10 characters")
    private String name;
}
