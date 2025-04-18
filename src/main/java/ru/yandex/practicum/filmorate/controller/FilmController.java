package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @PostMapping
    public Film add(@RequestBody Film film) {
        validation(film);
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        validation(film);
    }

    @GetMapping
    public Collection<Film> getAll() {
        return films.values();
    }

    private void validation(@RequestBody Film valFilm){
        if (valFilm.getName()==null){
            throw new ValidationException("Название не может быть пустым");
        }
        if (valFilm.getDescription().length()>200){
            throw new ValidationException("Максимальная длина описания - 200 символов");
        }
        if (valFilm.getReleaseDate().isBefore(LocalDate.of(1895, 12,28))){
            throw new ValidationException("Дата релиза - не раньше 28 декабря 1895 года");
        }
        if (valFilm.getDuration().toMinutes()<0){
            throw new ValidationException("Продолжительность должна быть положительной");
        }
    }
}
