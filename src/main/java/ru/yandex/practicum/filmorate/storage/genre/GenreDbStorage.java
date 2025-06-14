package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Repository
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Genre getGenreById(int id) {
        String sql = "SELECT * FROM genre WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new GenreRowMapper(), id);
        } catch (Exception ex) {
            throw new NotFoundException("Жанр с id=" + id + " не найден");
        }
    }

    @Override
    public List<Genre> getAllGenres() {
        String sql = "SELECT * FROM genre";
        List<Genre> genres = jdbcTemplate.query(sql, new GenreRowMapper());
        return genres;
    }

    @Override
    public void deleteAllGenres() {
        String sql = "DELETE FROM genre";
        jdbcTemplate.update(sql);
    }
}
