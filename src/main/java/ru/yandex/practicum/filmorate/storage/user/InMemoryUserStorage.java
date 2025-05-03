package ru.yandex.practicum.filmorate.storage.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int idCounter = 0;

    @Override
    public User addUser(User user) {
        user.setId(++idCounter);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User newUser) {
        if (!users.containsKey(newUser.getId())) {
            throw new NotFoundException("Пользователя с указанным ID не существует");
        }
        users.put(newUser.getId(), newUser);
        return newUser;
    }

    @Override
    public List<User> getAllUsers() {
        List<User> allUsers = new ArrayList<>(users.values());
        return allUsers;
    }

    @Override
    public void deleteUser(int id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с данным ID не найден");
        }
        users.remove(id);
    }

    @Override
    public User getUserById(int id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с данным ID не найден");
        }
        return users.get(id);
    }
}
