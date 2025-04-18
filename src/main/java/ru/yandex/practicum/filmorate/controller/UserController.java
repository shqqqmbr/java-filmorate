package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private final Logger log = LoggerFactory.getLogger(UserController.class);

    @PostMapping
    public User add(@RequestBody User user) {
        log.info("Получен запрос на добавление нового пользователя");
        user.validation();
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Новый пользователь {} добавлен", user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        log.info("Получен запрос на обновление пользователя");
        if (!users.containsKey(newUser.getId())) {
            log.error("Пользователь с ID {} не найден", newUser.getId());
            throw new ValidationException("Пользователя с указанным ID не существует");
        }
        newUser.validation();
        users.put(newUser.getId(), newUser);
        log.info("Пользователь обновлен на нового: {}", newUser);
        return newUser;
    }

    @GetMapping
    public List<User> getAll() {
        log.info("Получен запрос на получение всех пользователей");
        log.info("Список всех пользователей получен");
        return new ArrayList<>(users.values());
    }

    private int getNextId() {
        int currentId = users.keySet().stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentId;
    }
}
