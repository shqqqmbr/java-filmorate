package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Set;

@SpringBootTest
public class FilmTest {
    private Validator validator;
    private Film film;

    @BeforeEach
    public void beforeEach() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        film = new Film();
        film.setId(1);
        film.setName("Фильм");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(100);
    }

    @Test
    public void emptyNameTest() {
        film.setName("");
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        Assertions.assertFalse(violations.isEmpty());
        Assertions.assertEquals("Название не может быть пустым",
                violations.iterator().next().getMessage());
    }

    @Test
    public void lengthOfDescriptionLess200() {
        String longDescription = "f".repeat(201);
        film.setDescription(longDescription);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        Assertions.assertFalse(violations.isEmpty());
        Assertions.assertEquals("Максимальная длина описания - 200 символов",
                violations.iterator().next().getMessage());
    }

    @Test
    public void releaseDateNotBefore1895() {
        film.setReleaseDate(LocalDate.of(1895, 12, 20));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        Assertions.assertFalse(violations.isEmpty());
        Assertions.assertEquals("Дата релиза - не раньше 28 декабря 1895 года",
                violations.iterator().next().getMessage());
    }

    @Test
    public void durationIsPositive() {
        film.setDuration(-10);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        Assertions.assertFalse(violations.isEmpty());
        Assertions.assertEquals("Продолжительность должна быть больше 0",
                violations.iterator().next().getMessage());
    }
}
