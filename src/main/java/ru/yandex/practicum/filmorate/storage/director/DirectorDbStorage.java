package ru.yandex.practicum.filmorate.storage.director;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.DirectorRowMapper;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Repository
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DirectorDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Director> getAllDirectors() {
        log.info("Успешно возвращаем список всех режиссеров");
        String sql = "SELECT * FROM directors";
        return jdbcTemplate.query(sql, new DirectorRowMapper());
    }

    @Override
    public Director getDirectorById(int id) {
        try {
            String sql = "SELECT * FROM directors WHERE director_id = ?";
            Director director = jdbcTemplate.queryForObject(sql, new DirectorRowMapper(), id);
            log.info("Успешно возвращаем режиссера с id: {}", id);
            return director;
        } catch (Exception ex) {
            throw new NotFoundException("Режиссер с id=" + id + " не найден");
        }
    }

    @Override
    public Director addDirector(Director director) {
        String sql = "INSERT INTO directors (name) VALUES (?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setObject(1, director.getName());
            return ps;
        }, keyHolder);

        Integer id = keyHolder.getKeyAs(Integer.class);

        if (id != null) {
            director.setId(id);
        } else {
            log.warn("Не получилось добавить режиссера в базу данных");
            throw new RuntimeException("Не удалось сохранить данные");
        }

        log.info("Успешно добавили режиссера с id {} в базу данных", id);
        return director;
    }

    @Override
    public Director updateDirector(Director newDirector) {
        try{
            String sql = "UPDATE directors SET name = ? WHERE director_id = ?";
            int rowsUpdated = jdbcTemplate.update(sql, newDirector.getName(), newDirector.getId());
            if (rowsUpdated == 0) {
                log.warn("Не получилось обновить режиссера с id: {}", newDirector.getId());
                throw new RuntimeException("Не удалось обновить данные");
            }
            log.info("Успешно обновляем в базе данных режиссера с id: {}", newDirector.getId());
            return newDirector;
        } catch (Exception ex) {
            throw new NotFoundException("Режиссер с id=" + newDirector.getId() + " не найден");
        }
    }

    @Override
    public void deleteDirector(int id) {
        try {
            String sql = "DELETE FROM directors WHERE director_id = ?";
            jdbcTemplate.update(sql, id);
            log.info("Директор с id={} удален", id);
        } catch (Exception ex){
            throw new NotFoundException("Режиссер с id=" + id + " не найден");
        }
    }

    @Override
    public Set<Director> getFilmDirectors(int filmId) {
        String sql = "SELECT * FROM directors WHERE director_id IN (SELECT director_id "
                + "FROM film_directors WHERE film_id = ?)";
        return new HashSet<>(jdbcTemplate.query(sql, new DirectorRowMapper(), filmId));
    }

    private void checkDirectorPresence(int id) {
        String checkSql = "SELECT COUNT(*) FROM directors WHERE director_id = ?";
        Integer counter = jdbcTemplate.queryForObject(checkSql, Integer.class, id);
        if (counter == null || counter == 0) {
            throw new NotFoundException("Режиссер с id=" + id + " не найден");
        }
    }
}
