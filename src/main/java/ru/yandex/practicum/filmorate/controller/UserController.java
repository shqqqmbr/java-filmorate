package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Valid
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private final Logger log = LoggerFactory.getLogger(UserController.class);
    private int idCounter = 0;

    @PostMapping
    public User add(@Valid @RequestBody User user) {
        log.info("Получен запрос на добавление нового пользователя");
        user.setId(++idCounter);
        users.put(user.getId(), user);
        log.info("Новый пользователь {} добавлен", user);
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.info("Получен запрос на обновление пользователя");
        if (!users.containsKey(newUser.getId())) {
            log.error("Пользователь с ID {} не найден", newUser.getId());
            throw new ValidationException("Пользователя с указанным ID не существует");
        }
        users.put(newUser.getId(), newUser);
        log.info("Пользователь обновлен на нового: {}", newUser);
        return newUser;
    }

    @GetMapping
    public List<User> getAll() {
        log.info("Получен запрос на получение всех пользователей");
        List<User> allUsers = new ArrayList<>(users.values());
        log.info("Список всех пользователей получен");
        return allUsers;
    }
}
