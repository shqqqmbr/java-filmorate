package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.controller.marker.Marker;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public List<Director> getAll() {
        log.info("Получен http-запрос на получение списка всех режиссеров");
        return directorService.getAllDirectors();
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable("id") @Positive int id) {
        log.info("Получен http-запрос на получение режиссера с id: {}", id);
        return directorService.getDirectorById(id);
    }

    @PostMapping
    @Validated({Marker.OnCreate.class})
    public Director add(@Valid @RequestBody Director director) {
        log.info("Получен http-запрос на добавление режиссера");
        return directorService.addDirector(director);
    }

    @PutMapping
    @Validated({Marker.OnUpdate.class})
    public Director update(@RequestBody Director newDirector) {
        log.info("Получен http-запрос на обновление режиссера с id: {}", newDirector.getId());
        return directorService.updateDirector(newDirector);
    }

    @DeleteMapping("/{id}")
    public void deleteDirectorById(@PathVariable("id") @Positive int id) {
        log.info("Получен http-запрос на удаление режиссера с id: {}", id);
        directorService.deleteDirector(id);
    }
}
