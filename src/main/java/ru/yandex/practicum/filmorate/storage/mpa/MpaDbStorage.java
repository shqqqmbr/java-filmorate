package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Repository
public class MpaDbStorage implements MpaStorage {
    private JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Mpa getMpaById(int id) {
        String sql = "SELECT * FROM mpa WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new MpaRowMapper(), id);
        } catch (Exception ex) {
            throw new NotFoundException("Mpa с id=" + id + " не найден");
        }
    }

    @Override
    public List<Mpa> getAllMpas() {
        String sql = "SELECT * FROM mpa";
        List<Mpa> mpas = jdbcTemplate.query(sql, new MpaRowMapper());
        return mpas;
    }

    @Override
    public void deleteAllMpas() {
        String sql = "DELETE FROM mpa";
        jdbcTemplate.update(sql);
    }
}
