package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Stream;

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
    public List<User> getAllUsers() {
        log.info("Получен запрос на получение всех пользователей");
        List<User> allUsers = new ArrayList<>(users.values());
        log.info("Список всех пользователей получен");
        return allUsers;
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable int id) {
        return users.get(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> addFriend(@PathVariable int id, @PathVariable int friendId) {
        if (!users.containsKey(id) || !users.containsKey(friendId)) {
            return ResponseEntity.notFound().build();
        }
        User user = users.get(id);
        user.getFriends().add(friendId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> deleteFriend(@PathVariable int id, @PathVariable int friendId) {
        if (!users.containsKey(id) || !users.containsKey(friendId)) {
            return ResponseEntity.notFound().build();
        }
        User user = users.get(id);
        Set<Integer> friends = user.getFriends();
        friends.remove(friendId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/friends")
    public List<User> getAllFriends(@PathVariable int id) {
        if (!users.containsKey(id)) {
            throw new NullPointerException("Пользователя с данным ID не существует");
        }
        return users.get(id).getFriends().stream()
                .map(friendId -> users.get(friendId))
                .filter(Objects::nonNull)
                .toList();
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable int id, @PathVariable int otherId) {
        if (!users.containsKey(id) || !users.containsKey(otherId)) {
            throw new NullPointerException("Пользователь не найден");
        }
        User user = users.get(id);
        User otherUser = users.get(otherId);
        Set<Integer> commonFriends = new HashSet<>();
        commonFriends.addAll(user.getFriends());
        commonFriends.addAll(otherUser.getFriends());
        return commonFriends.stream()
                .map(commonId -> users.get(commonId))
                .filter(Objects::nonNull)
                .toList();
    }
}
