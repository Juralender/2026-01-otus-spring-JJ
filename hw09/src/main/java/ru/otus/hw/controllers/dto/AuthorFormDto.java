package ru.otus.hw.controllers.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.otus.hw.services.dto.AuthorCreateDto;
import ru.otus.hw.services.dto.AuthorDto;
import ru.otus.hw.services.dto.AuthorUpdateDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorFormDto {

    private long id;

    @NotBlank(message = "Full name must not be empty")
    @Size(max = 25, message = "Full name must not exceed 25 characters")
    private String fullName;

    public static AuthorFormDto fromDto(AuthorDto author) {
        return new AuthorFormDto(author.getId(), author.getFullName());
    }

    public AuthorCreateDto toCreateDto() {
        return new AuthorCreateDto(fullName);
    }

    public AuthorUpdateDto toUpdateDto(long id) {
        return new AuthorUpdateDto(id, fullName);
    }
}
