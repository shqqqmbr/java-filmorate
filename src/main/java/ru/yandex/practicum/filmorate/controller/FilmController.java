package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private final Logger log = LoggerFactory.getLogger(FilmController.class);

    @PostMapping
    public Film add(@RequestBody Film film) {
        log.info("Получен запрос на добавление фильма");
        film.validation();
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Добавлен фильм: {}", film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        log.info("Получен запрос на обновление фильма");
        if (!films.containsKey(newFilm.getId())){
            log.error("Фильм с ID {} не найден", newFilm.getId());
            throw new ValidationException("Фильма с указанным ID не существует");
        }
        newFilm.validation();
        films.put(newFilm.getId(), newFilm);
        log.info("Фильм обновлен на новый: {}", newFilm);
        return newFilm;
    }

    @GetMapping
    public List<Film> getAll() {
        log.info("Получен запрос на получение всех фильмов");
        log.info("Список всех фильмов получен");
        return new ArrayList<>(films.values());
    }

    private int getNextId() {
        int currentId = films.keySet().stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentId;
    }
}
