package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();

    @PostMapping
    public User add(@RequestBody User user) {

    }

    @PutMapping
    public User update(@RequestBody User user) {

    }

    @GetMapping
    public Collection<User> getAll() {

    }
}
