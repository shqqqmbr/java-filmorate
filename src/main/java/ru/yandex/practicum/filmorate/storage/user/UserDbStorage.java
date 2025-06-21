package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User addUser(User user) {
        String sql = "INSERT INTO USERS (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            user.setId(keyHolder.getKey().intValue());
        } else {
            throw new RuntimeException("Не удалось получить сгенерированный ID");
        }
        return user;
    }

    @Override
    public User updateUser(User newUser) {
        checkUserPresence(newUser.getId());
        String sql = "UPDATE USERS SET email=?, login=?, name=?, birthday=? WHERE id=?";
        jdbcTemplate.update(
                sql,
                newUser.getEmail(),
                newUser.getLogin(),
                newUser.getName(),
                Date.valueOf(newUser.getBirthday()),
                newUser.getId()
        );
        return newUser;
    }

    @Override
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM USERS";
        return jdbcTemplate.query(sql, new UserRowMapper());
    }

    @Override
    public List<User> getCommonFriends(int userOneId, int userTwoId) {
        checkUserPresence(userOneId);
        checkUserPresence(userTwoId);
        String sql = """
                SELECT * FROM USERS u
                JOIN friends f1 ON u.id = f1.friend_id AND f1.user_id = ?
                JOIN friends f2 ON u.id = f2.friend_id AND f2.user_id = ?
                """;
        List<User> commonFriends = jdbcTemplate.query(sql, new UserRowMapper(), userOneId, userTwoId);
        return commonFriends;
    }

    @Override
    public void deleteUser(int id) {
        checkUserPresence(id);
        String sql = "DELETE FROM USERS WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public User getUserById(int userId) {
        checkUserPresence(userId);
        String sql = "SELECT * FROM USERS WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new UserRowMapper(), userId);
    }

    @Override
    public void addFriend(int userId, int frienId) {
        checkUserPresence(userId);
        checkUserPresence(frienId);
        String sql = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, frienId, true);
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        checkUserPresence(userId);
        checkUserPresence(friendId);
        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public List<User> getAllFriends(int userId) {
        checkUserPresence(userId);
        String sql = "SELECT * FROM users JOIN friends ON users.id = friends.friend_id WHERE friends.user_id = ?";
        return jdbcTemplate.query(sql, new UserRowMapper(), userId);
    }

    @Override
    public boolean isFriend(int userId, int friendId) {
        String sql = "SELECT * FROM friends WHERE user_id = ? AND friend_id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, userId, friendId);
        return rowSet.next();
    }

    private void checkUserPresence(int userId) {
        String checkSql = "SELECT COUNT(*) FROM USERS WHERE id = ?";
        int count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId);
        if (count == 0) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }
}
