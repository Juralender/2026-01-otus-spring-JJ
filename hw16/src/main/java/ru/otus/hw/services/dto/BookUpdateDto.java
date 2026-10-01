package ru.otus.hw.services.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookUpdateDto {

    private long id;

    @NotBlank(message = "Title must not be empty")
    @Size(max = 30, message = "Title must not exceed 30 characters")
    private String title;

    @Positive(message = "Author must be selected")
    private long authorId;

    @NotEmpty(message = "At least one genre must be selected")
    private Set<Long> genreIds;
}
