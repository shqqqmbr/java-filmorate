package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
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
        String checkSql = "SELECT COUNT(*) FROM USERS WHERE id = ?";
        int counter = jdbcTemplate.queryForObject(checkSql, Integer.class, newUser.getId());
        if (counter == 0) {
            throw new NotFoundException("Пользователь с id=" + newUser.getId() + " не найден");
        }

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
        List<User> users = jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
            return user;
        });
        return users;
    }

    @Override
    public List<User> getCommonFriends(int userOneId, int userTwoId) {
        String sql = "SELECT * FROM USERS u"
                + "JOIN friends f1 ON u.id = f1.friend_id AND f1.user_id = ?"
                + "JOIN friends f2 ON u.id = f2.friend_id AND f2.user_id = ?";
        List<User> commonFriends = jdbcTemplate.query(sql, new Object[]{userOneId, userTwoId}, new UserRowMapper());
        return commonFriends;
    }

    @Override
    public void deleteUser(int id) {
        String checkSql = "SELECT * FROM USERS WHERE id = ?";
        int counter = jdbcTemplate.queryForObject(checkSql, Integer.class, id);
        if (counter == 0) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        String sql = "DELETE FROM USERS WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public User getUserById(int id) {
        String sql = "SELECT * FROM USERS WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new UserRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
    }

    @Override
    public void addFriend(int userId, int frienId) {
        String checkSql = "SELECT COUNT(*) FROM friends WHERE"
                + "(user_id =  ? AND friend_id = ?)";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, frienId, frienId, userId);
        if (count > 0) {
            throw new IllegalStateException("Дружба между пользователями уже существует");
        }
        String sql = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, frienId, true);
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public List<User> getAllFriends(int userId) {
        String sql = "SELECT * FROM friends WHERE user_id = ?";
        List<User> friends = jdbcTemplate.query(sql, new UserRowMapper(), userId);
        return friends;
    }

    @Override
    public boolean isFriend(int userId, int friendId) {
        String sql = "SELECT * FROM friends WHERE user_id = ? AND friend_id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, userId, friendId);
        return rowSet.next();
    }
}
