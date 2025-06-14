package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@RestController
@RequestMapping("/genre")
@RequiredArgsConstructor
@Valid
public class GenreController {
    private final GenreService service;

    @GetMapping("/{id}")
    public Genre getGenreById(int id) {
        return service.getGenreById(id);
    }

    @GetMapping
    public List<Genre> getAllGenres() {
        return service.getAllGenres();
    }

    @DeleteMapping
    public void deleteAllGenres() {
        service.deleteAllGenres();
    }
}
