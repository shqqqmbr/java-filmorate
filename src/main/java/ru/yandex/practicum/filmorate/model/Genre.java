package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class Genre {
    @Positive
    private int genreId;
    @NotBlank
    private String genreName;
}
