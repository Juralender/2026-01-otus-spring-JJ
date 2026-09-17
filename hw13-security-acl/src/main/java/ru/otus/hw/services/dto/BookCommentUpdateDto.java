package ru.otus.hw.services.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookCommentUpdateDto {

    private long id;

    @NotBlank(message = "Comment text must not be empty")
    private String text;
}
