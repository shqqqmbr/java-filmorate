package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.Duration;
import java.time.LocalDate;


@Data
public class Film {
    private int id;
    @NotBlank
    private String name;
    @Size(max = 200, message = "Максимальная длина описания - 200 сиимволов")
    private String description;
    @NotNull(message = "Дата релиза должна быть указана")
    private LocalDate releaseDate;
    @Positive(message = "Продолжительность должна быть больше 0")
    private Duration duration;

    private final Logger log = LoggerFactory.getLogger(Film.class);

    public void validation() {
        if (releaseDate.isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Дата релиза {} некорректна", releaseDate);
            throw new ValidationException("Дата релиза - не раньше 28 декабря 1895 года");
        }
    }
}
