package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    public User addUser(User user);

    public User updateUser(User newUser);

    public List<User> getAllUsers();

    public void deleteUser(int id);

    public User getUserById(int id);
}
