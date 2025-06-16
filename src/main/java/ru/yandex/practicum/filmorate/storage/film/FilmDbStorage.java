package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

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
        int mpaId = film.getMpa().getId();
        boolean mpaExists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM MPA WHERE mpa_id = ?",
                Integer.class, mpaId) > 0;
        if (!mpaExists) {
            throw new NotFoundException("MPA с id=" + mpaId + " не найден");
        }

        Set<Genre> genreSet = film.getGenres();
        List<Integer> filmsGenreId = genreSet.stream()
                .map(Genre::getId)
                .collect(Collectors.toList());
        String checGenresSql = "SELECT genre_id FROM genres";
        List<Integer> genresIdsFromGenres = jdbcTemplate.queryForList(checGenresSql, Integer.class);
        List<Integer> missingIds = filmsGenreId.stream()
                .filter(id -> !genresIdsFromGenres.contains(id))
                .collect(Collectors.toList());
        if (!missingIds.isEmpty()) {
            throw new NotFoundException("ID жанров не найдены в базе: " +  missingIds);
        }

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, mpaId);
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            film.setId(keyHolder.getKey().intValue());
        } else {
            throw new RuntimeException("Не удалось получить сгенерированный ID");
        }
        addGenre(film.getId(),  genreSet);
        return film;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        String checkSql = "SELECT COUNT(*) FROM FILMS WHERE id = ?";
        Integer counter = jdbcTemplate.queryForObject(checkSql, Integer.class, newFilm.getId());
        if (counter == 0) {
            throw new NotFoundException("Фильм с id=" + newFilm.getId() + " не найден");
        }
        int mpaId = newFilm.getMpa().getId();
        boolean mpaExists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM MPA WHERE mpa_id = ?",
                Integer.class, mpaId) > 0;
        if (!mpaExists) {
            throw new NotFoundException("MPA с id=" + mpaId + " не найден");
        }
        String sql = "UPDATE FILMS SET name = ?, description = ?, release_date = ?, duration = ?, mpa = ? WHERE id = ?";
        jdbcTemplate.update(
                sql,
                newFilm.getName(),
                newFilm.getDescription(),
                Date.valueOf(newFilm.getReleaseDate()),
                newFilm.getDuration(),
                mpaId,
                newFilm.getId()
        );
        addGenre(newFilm.getId(), newFilm.getGenres());
        newFilm.setGenres(newFilm.getGenres());
        return newFilm;
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = """
        SELECT 
            f.id,
            f.name,
            f.description,
            f.release_date,
            f.duration,
            f.mpa AS mpa_id,
            m.mpa_name
        FROM films f
        LEFT JOIN mpa m ON f.mpa = m.mpa_id
        """;

        return jdbcTemplate.query(sql, new FilmRowMapper());
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
        String sql = "SELECT * FROM FILMS JOIN mpa ON films.mpa = mpa.mpa_id WHERE id = ?";
        int counter = jdbcTemplate.queryForObject(sql, Integer.class, id);
        if (counter == 0) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        } else {
            return jdbcTemplate.queryForObject(sql, new FilmRowMapper(), id);
        }
    }

    @Override
    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void deleteLike(int filmId, int userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = "SELECT * FROM films "
                + "LEFT JOIN genres ON films.genres = genres.genre_id "
                + "JOIN mpa ON films.mpa = mpa.mpa_id "
                + "GROUP BY films.id "
                + "ORDER BY COUNT(likes.film_id) DESC "
                + "LIMIT " + count;
        List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper(), count);
        return films;
    }

    public void addGenre(int filmId, Set<Genre> genres){
        if (genres == null || genres.isEmpty()) {
            return;
        }
        boolean filmExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM films WHERE id = ?",
                Integer.class, filmId) > 0;
        if (!filmExists) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
        List<Integer> genreIds = genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toList());
        String checkGenresSql = "SELECT genre_id FROM genres";
        List<Integer> existingGenreIds = jdbcTemplate.queryForList(checkGenresSql, Integer.class);
        List<Integer> missingIds = genreIds.stream()
                .filter(id -> !existingGenreIds.contains(id))
                .collect(Collectors.toList());
        if (!missingIds.isEmpty()) {
            throw new NotFoundException("Жанры с id=" + missingIds + " не найдены в справочнике");
        }
        String sql = "INSERT INTO film_genres (film_id, genre_id) SELECT ?, genre_id FROM genres";
        jdbcTemplate.update(sql, filmId);
    }

    private Film createFilm(ResultSet rs, int id) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        film.setGenres(new HashSet<>());
        film.setMpa(new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name")));
        return film;
    }
}
