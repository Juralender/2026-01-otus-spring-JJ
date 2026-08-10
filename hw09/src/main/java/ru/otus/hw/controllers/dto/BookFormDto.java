package ru.otus.hw.controllers.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookFormDto {

    private long id;

    @NotBlank(message = "Title must not be empty")
    @Size(max = 30, message = "Title must not exceed 30 characters")
    private String title;

    private long authorId;

    private Set<Long> genreIds;

    public static BookFormDto fromBook(Book book) {
        return new BookFormDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor().getId(),
                book.getGenres().stream().map(Genre::getId).collect(Collectors.toSet()));
    }
}
