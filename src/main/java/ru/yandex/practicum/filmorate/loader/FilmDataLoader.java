package ru.yandex.practicum.filmorate.loader;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@AllArgsConstructor
public class FilmDataLoader {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public void loadFilmAdditionalData(Film film) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("filmId", film.getId());

        String sql = "SELECT " +
                "l.user_id AS like_user_id, " +
                "fg.genre_id, g.genre_name AS genre_name, " +
                "fd.director_id, d.name AS director_name " +
                "FROM films f " +
                "LEFT JOIN likes l ON f.id = l.film_id " +
                "LEFT JOIN film_genres fg ON f.id = fg.film_id " +
                "LEFT JOIN genres g ON fg.genre_id = g.genre_id " +
                "LEFT JOIN film_directors fd ON f.id = fd.film_id " +
                "LEFT JOIN directors d ON fd.director_id = d.director_id " +
                "WHERE f.id = :filmId";
        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(sql, parameters);
        Set<Integer> likes = new HashSet<>();
        Set<Genre> genres = new HashSet<>();
        Set<Director> directors = new HashSet<>();
        for (Map<String, Object> row : rows) {
            if (row.get("like_user_id") != null) {
                likes.add((Integer) row.get("like_user_id"));
            }
            if (row.get("genre_id") != null) {
                Genre genre = new Genre();
                genre.setId((Integer) row.get("genre_id"));
                genre.setName((String) row.get("genre_name"));
                genres.add(genre);
            }
            if (row.get("director_id") != null) {
                Director director = new Director();
                director.setId((Integer) row.get("director_id"));
                director.setName((String) row.get("director_name"));
                directors.add(director);
            }
        }
        film.setLikes(likes);
        film.setGenres(genres);
        film.setDirectors(directors);
    }
}
