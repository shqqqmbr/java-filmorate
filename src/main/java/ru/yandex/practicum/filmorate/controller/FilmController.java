package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@RestController
@RequestMapping("/films")
@Valid
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private final Logger log = LoggerFactory.getLogger(FilmController.class);
    private int idCounter = 0;

    @PostMapping
    public Film add(@Valid @RequestBody Film film) {
        log.info("Получен запрос на добавление фильма");
        film.setId(++idCounter);
        films.put(film.getId(), film);
        log.info("Добавлен фильм: {}", film);
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        if (!films.containsKey(newFilm.getId())) {
            log.error("Фильм с id {} не найден", newFilm.getId());
            throw new ValidationException("Фильм с указанным id не существует");
        }
        films.put(newFilm.getId(), newFilm);
        log.info("Фильм обновлен на новый: {}", newFilm);
        return newFilm;
    }

    @GetMapping
    public List<Film> getAll() {
        log.info("Получен запрос на получение всех фильмов");
        List<Film> allFilms = new ArrayList<>(films.values());
        log.info("Список всех фильмов получен");
        return allFilms;
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> putLike(@PathVariable int id, @PathVariable int userId) {
        if (!films.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }
        films.get(id).getLikes().add(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> deleteLike(@PathVariable int id, @PathVariable int userId) {
        if (!films.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }
        films.get(id).getLikes().remove(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/popular?count={count}")
    public List<Film> getPopularFilms(@RequestParam(required = false, defaultValue = "10") int count) {
        return films.values().stream()
                .filter(film -> film.getLikes() != null)
                .sorted(Comparator.comparingInt(film -> -film.getLikes().size()))
                .limit(count)
                .toList();
    }
}
