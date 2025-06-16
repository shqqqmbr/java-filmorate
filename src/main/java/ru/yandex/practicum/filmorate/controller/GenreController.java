package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
@Valid
public class GenreController {
    private final GenreService service;

    @GetMapping("/{id}")
    public Genre getGenreById(@PathVariable("id") Integer id) {
        return service.getGenreById(id);
    }

    @GetMapping
    public List<Genre> getAllGenres() {
        return service.getAllGenres();
    }
}
