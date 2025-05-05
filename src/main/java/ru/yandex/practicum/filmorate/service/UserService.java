package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        User user = storage.getUserById(id);
        User friend = storage.getUserById(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(id);

        storage.updateUser(user);
        storage.updateUser(friend);
    }

    public void deleteFriend(int id, int friendId) {
        User user = storage.getUserById(id);
        User friend = storage.getUserById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(id);

        storage.updateUser(user);
        storage.updateUser(friend);
    }

    public List<User> getAllFriends(int id) {
        User user = storage.getUserById(id);
        List<User> friends = new ArrayList<>();
        for (Integer friendId : user.getFriends()) {
            storage.getUserById(friendId);
            friends.add(storage.getUserById(friendId));
        }
        return friends;
    }

    public List<User> getCommonFriends(int id, int otherId) {
        Set<Integer> userFriends = new HashSet<>(storage.getUserById(id).getFriends());
        Set<Integer> otherFriends = new HashSet<>(storage.getUserById(otherId).getFriends());

        userFriends.retainAll(otherFriends);

        List<User> commonFriends = new ArrayList<>();
        for (Integer friendId : userFriends) {
            storage.getUserById(friendId);
            commonFriends.add(storage.getUserById(friendId));
        }
        return commonFriends;
    }
}
