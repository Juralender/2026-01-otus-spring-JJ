package ru.otus.hw.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("genres")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Genre {

    @Id
    private long id;

    @Column("name")
    private String name;
}
