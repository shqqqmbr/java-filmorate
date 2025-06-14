package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
@Valid
public class MpaController {
    private final MpaService service;

    @GetMapping("/{id}")
    public Mpa getMpaById(int id) {
        return service.getMpaById(id);
    }

    @GetMapping
    public List<Mpa> getAllMpas() {
        return service.getAllMpas();
    }

    @DeleteMapping
    public void deleteAllMpas() {
        service.deleteAllMpas();
    }
}
