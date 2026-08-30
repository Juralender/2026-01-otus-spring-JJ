package ru.otus.hw.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("books_genres")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookGenreRelation {

    @Column("book_id")
    private long bookId;

    @Column("genre_id")
    private long genreId;
}
