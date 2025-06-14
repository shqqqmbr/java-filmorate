package ru.yandex.practicum.filmorate.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Genre {
    @Id
    private int genreId;
    private String genreName;
}
