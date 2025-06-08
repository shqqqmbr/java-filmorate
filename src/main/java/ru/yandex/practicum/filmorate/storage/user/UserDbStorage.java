package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Qualifier
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
        user.setId(keyHolder.getKey().intValue());
        return user;
    }

    @Override
    public User updateUser(User newUser) {
        String checkSql = "SELECT * FROM USERS WHERE id = ?";
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
                newUser.getBirthday(),
                newUser.getId()
        );
        return getUserById(newUser.getId());
    }

    @Override
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM USERS";
        List<User> users = jdbcTemplate.query(sql, (rs, rowNum) -> new User());
        return users;
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
        int counter = jdbcTemplate.queryForObject(sql, Integer.class, id);
        if (counter == 0) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        } else {
            return jdbcTemplate.queryForObject(sql, new UserRowMapper(), id);
        }
    }
}
