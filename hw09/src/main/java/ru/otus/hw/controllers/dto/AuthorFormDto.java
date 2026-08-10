package ru.otus.hw.controllers.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.otus.hw.models.Author;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorFormDto {

    private long id;

    @NotBlank(message = "Full name must not be empty")
    @Size(max = 25, message = "Full name must not exceed 25 characters")
    private String fullName;

    public static AuthorFormDto fromAuthor(Author author) {
        return new AuthorFormDto(author.getId(), author.getFullName());
    }
}
