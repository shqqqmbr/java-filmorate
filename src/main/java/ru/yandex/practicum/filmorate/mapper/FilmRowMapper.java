package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class FilmRowMapper implements RowMapper<Film> {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public FilmRowMapper(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getInt("id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        film.setReleaseDate(resultSet.getDate("release_date").toLocalDate());
        film.setDuration(resultSet.getInt("duration"));

        Mpa mpa = new Mpa();
        mpa.setId(resultSet.getInt("mpa_id"));
        mpa.setName(resultSet.getString("mpa_name"));
        film.setMpa(mpa);

        Set<Genre> genres = getGenresForFilm(film.getId());
        film.setGenres(genres);

        Set<Integer> likes = getLikesForFilm(film.getId());
        film.setLikes(likes);

        Set<Director> directors = getDirectorsForFilm(film.getId());
        film.setDirectors(directors);

        return film;
    }

    private Set<Genre> getGenresForFilm(long filmId) {
        String sql = """
                SELECT g.genre_id, g.genre_name
                FROM genres g
                    JOIN film_genres fg ON g.genre_id = fg.genre_id
                WHERE fg.film_id = :filmId
                ORDER BY g.genre_id ASC
                """;
        return new LinkedHashSet<>(namedParameterJdbcTemplate.query(
                sql,
                new MapSqlParameterSource("filmId", filmId),
                new GenreRowMapper()
        ));
    }

    private Set<Integer> getLikesForFilm(int filmId) {
        String sql = """
                SELECT user_id
                FROM likes
                WHERE film_id = :filmId
                """;
        return new HashSet<>(namedParameterJdbcTemplate.queryForList(
                sql,
                new MapSqlParameterSource("filmId", filmId),
                Integer.class
        ));
    }

    private Set<Director> getDirectorsForFilm(int filmId) {
        String sql = "SELECT d.* FROM directors d " +
                "JOIN film_directors fd ON d.director_id = fd.director_id " +
                "WHERE fd.film_id = :filmId";

        Map<String, Object> params = Collections.singletonMap("filmId", filmId);
        return new LinkedHashSet<>(namedParameterJdbcTemplate.query(sql, params, new DirectorRowMapper()));
    }
}