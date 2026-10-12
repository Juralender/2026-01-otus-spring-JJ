package ru.otus.hw.services.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorCreateDto {

    @NotBlank(message = "Full name must not be empty")
    @Size(max = 25, message = "Full name must not exceed 25 characters")
    private String fullName;
}
