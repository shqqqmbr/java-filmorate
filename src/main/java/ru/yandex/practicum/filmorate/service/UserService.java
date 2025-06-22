package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage storage;

    public User addUser(User user) {
        return storage.addUser(user);
    }

    public User updateUser(User user) {
        return storage.updateUser(user);
    }

    public void deleteUser(int id) {
        storage.deleteUser(id);
    }

    public List<User> getAllUsers() {
        return storage.getAllUsers();
    }


    public User getUserById(int id) {
        return storage.getUserById(id);
    }

    public void addFriend(int id, int friendId) {
        storage.addFriend(id, friendId);
    }

    public void deleteFriend(int id, int friendId) {
        storage.deleteFriend(id, friendId);
    }

    public List<User> getAllFriends(int id) {
        return storage.getAllFriends(id);
    }

    public List<User> getCommonFriends(int id, int otherId) {
        return storage.getCommonFriends(id, otherId);
    }
}
