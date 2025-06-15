package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film addFilm(Film film) {
        String sql = "INSERT INTO FILMS (name, description, release_date, duration, mpa) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getMpaId());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            film.setId(keyHolder.getKey().intValue());
        } else {
            throw new RuntimeException("Не удалось получить сгенерированный ID");
        }
        return film;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        String checkSql = "SELECT * FROM FILMS WHERE id = ?";
        int counter = jdbcTemplate.queryForObject(checkSql, Integer.class, newFilm.getId());
        if (counter == 0) {
            throw new NotFoundException("Фильм с id=" + newFilm.getId() + " не найден");
        }
        String sql = "UPDATE FILMS SET name = ?, description = ?, release_date = ?, duration = ?, mpa = ? WHERE id = ?";
        jdbcTemplate.update(
                sql,
                newFilm.getName(),
                newFilm.getDescription(),
                Date.valueOf(newFilm.getReleaseDate()),
                newFilm.getDuration(),
                newFilm.getMpa().getMpaId(),
                newFilm.getId()
        );
        return newFilm;
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT * FROM FILMS";
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> new Film());
        return films;
    }

    @Override
    public void deleteFilm(int id) {
        String checkSql = "SELECT * FROM FILMS WHERE id = ?";
        int counter = jdbcTemplate.queryForObject(checkSql, Integer.class, id);
        if (counter == 0) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        String sql = "DELETE FROM FILMS WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Film getFilmById(int id) {
        String sql = "SELECT * FROM FILMS WHERE id = ?";
        int counter = jdbcTemplate.queryForObject(sql, Integer.class, id);
        if (counter == 0) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        } else {
            return jdbcTemplate.queryForObject(sql, new FilmRowMapper(), id);
        }

    }
}
