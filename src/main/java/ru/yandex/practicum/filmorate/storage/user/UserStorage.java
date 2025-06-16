package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    User addUser(User user);

    User updateUser(User newUser);

    List<User> getAllUsers();

    void deleteUser(int id);

    User getUserById(int id);

    void addFriend(int friendId, int userId);

    void deleteFriend(int friendId, int userId);

    List<User> getAllFriends(int userId);

    List<User> getCommonFriends(int userOneId, int userTwoId);

    boolean isFriend(int userId, int friendId);
}
