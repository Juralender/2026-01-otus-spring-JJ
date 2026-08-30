package ru.otus.hw.models;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("book_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BookComment {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    private long id;

    @ToString.Include
    @Column("text")
    private String text;

    @Column("book_id")
    private long bookId;

    public BookComment(String text, long bookId) {
        this.text = text;
        this.bookId = bookId;
    }
}
