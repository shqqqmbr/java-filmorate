package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/films")
@Valid
public class FilmController {
    private final Logger log = LoggerFactory.getLogger(FilmController.class);
    public FilmService service;

    @Autowired
    public FilmController(FilmService service) {
        this.service = service;
    }

    @PostMapping
    public Film add(@Valid @RequestBody Film film) {
        return service.addFilm(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        return service.updateFilm(newFilm);
    }

    @GetMapping
    public List<Film> getAll() {
        return service.getAllFilms();
    }

    @PutMapping("/{id}/like/{userId}")
    public void putLike(@PathVariable("id") int id, @PathVariable("userId") int userId) {
        service.putLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable("id") int id, @PathVariable("userId") int userId) {
        service.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(required = false, defaultValue = "10") int count) {
        return service.getPopularFilms(count);
    }
}
