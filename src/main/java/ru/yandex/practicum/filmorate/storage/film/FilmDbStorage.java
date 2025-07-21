package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.enums.EventTypes;
import ru.yandex.practicum.filmorate.model.enums.OperationTypes;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final MpaDbStorage mpaDbStorage;
    private final UserDbStorage userStorage;
    private final DirectorDbStorage directorDbStorage;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate, UserDbStorage userStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.mpaDbStorage = new MpaDbStorage(jdbcTemplate);
        this.userStorage = userStorage;
        this.directorDbStorage = new DirectorDbStorage(jdbcTemplate);
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
            throw new NotFoundException("ID жанров не найдены в базе: " + missingIds);
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
        addGenre(film.getId(), genreSet);
        addDirectors(film.getId(), film.getDirectors());
        return getFilmById(film.getId());
    }

    @Override
    public Film updateFilm(Film newFilm) {
        checkFilmPresence(newFilm.getId());
        Mpa mpa = mpaDbStorage.getMpaById(newFilm.getMpa().getId());
        newFilm.setMpa(mpa);
        String sql = "UPDATE FILMS SET name = ?, description = ?, release_date = ?, duration = ?, mpa = ? WHERE id = ?";
        jdbcTemplate.update(
                sql,
                newFilm.getName(),
                newFilm.getDescription(),
                Date.valueOf(newFilm.getReleaseDate()),
                newFilm.getDuration(),
                newFilm.getMpa().getId(),
                newFilm.getId()
        );
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", newFilm.getId());
        jdbcTemplate.update("DELETE FROM film_directors WHERE film_id = ?", newFilm.getId());
        Set<Genre> sortedGenres = newFilm.getGenres().stream()
                .sorted(Comparator.comparingInt(Genre::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        newFilm.setGenres(sortedGenres);
        addGenre(newFilm.getId(), newFilm.getGenres());
        addDirectors(newFilm.getId(), newFilm.getDirectors());
        return getFilmById(newFilm.getId());
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration,"
                + "f.mpa AS mpa_id, m.mpa_name "
                + "FROM FILMS f "
                + "LEFT JOIN mpa m ON f.mpa = m.mpa_id";
        List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper(namedParameterJdbcTemplate));
        setFilmDirectors(films);
        return films;
    }

    @Override
    public void deleteFilm(int filmId) {
        checkFilmPresence(filmId);
        String sql = "DELETE FROM FILMS WHERE id = ?";
        jdbcTemplate.update(sql, filmId);
    }

    @Override
    public Film getFilmById(int filmId) {
        checkFilmPresence(filmId);
        String filmSql = "SELECT f.*, m.mpa_id, m.mpa_name AS mpa_name "
                + "FROM FILMS f "
                + "JOIN mpa m ON f.mpa = m.mpa_id "
                + "WHERE f.id = ? ";
        Film film = jdbcTemplate.queryForObject(filmSql, new FilmRowMapper(namedParameterJdbcTemplate), filmId);
        setFilmDirectors(film);
        return film;
    }

    @Override
    public void addLike(int filmId, int userId) {
        String checkSql = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        int count = jdbcTemplate.queryForObject(checkSql, Integer.class, filmId, userId);
        if (count > 0) {
            userStorage.addUserFeed(filmId, userId, EventTypes.LIKE, OperationTypes.ADD);
            return;
        }
        String insertSql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(insertSql, filmId, userId);
        userStorage.addUserFeed(filmId, userId, EventTypes.LIKE, OperationTypes.ADD);
    }

    @Override
    public void deleteLike(int filmId, int userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
        userStorage.addUserFeed(filmId, userId, EventTypes.LIKE, OperationTypes.REMOVE);
    }

    @Override
    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {
        String sql = """
                SELECT films.*, mpa.*, COUNT(likes.film_id) AS likes_count
                FROM films
                JOIN mpa ON films.mpa = mpa.mpa_id
                LEFT JOIN likes ON films.id = likes.film_id
                LEFT JOIN film_genres ON films.id = film_genres.film_id
                LEFT JOIN genres ON film_genres.genre_id = genres.genre_id
                LEFT JOIN film_directors ON films.id = film_directors.film_id
                LEFT JOIN directors ON film_directors.director_id = directors.director_id
                WHERE (:genreId IS NULL OR genres.genre_id = :genreId)
                AND (:year IS NULL OR YEAR(films.release_date) = :year)
                GROUP BY films.id, mpa.mpa_id
                ORDER BY likes_count DESC
                LIMIT :count
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("genreId", genreId)
                .addValue("year", year)
                .addValue("count", count);
        return namedParameterJdbcTemplate.query(sql, params, new FilmRowMapper(namedParameterJdbcTemplate));
    }

    @Override
    public List<Film> getSortedFilms(int directorId, String sortBy) {
        String checkSql = "SELECT COUNT(*) FROM directors WHERE director_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, directorId);
        if (count == 0) {
            throw new NotFoundException("Режиссер не найден");
        }
        String sqlLikes = """
                SELECT f.id, f.name, f.description, f.duration, f.release_date, mpa.*
                FROM films AS f
                JOIN mpa ON f.mpa = mpa.mpa_id
                LEFT JOIN film_directors AS fd ON f.id = fd.film_id
                LEFT JOIN likes AS l ON f.id = l.film_id
                WHERE fd.director_id = ?
                GROUP BY f.id
                ORDER BY COUNT(l.user_id) DESC
                """;
        String sqlYear = """
                SELECT f.id, f.name, f.description, f.duration, f.release_date, mpa.*
                FROM films AS f
                JOIN mpa ON f.mpa = mpa.mpa_id
                LEFT JOIN film_directors AS fd ON f.id = fd.film_id
                WHERE fd.director_id = ?
                ORDER BY f.release_date
                """;
        List<Film> directorFilms;
        if (sortBy.equalsIgnoreCase("likes")) {
            directorFilms = jdbcTemplate.query(sqlLikes, new FilmRowMapper(namedParameterJdbcTemplate), directorId);
        } else if (sortBy.equalsIgnoreCase("year")) {
            directorFilms = jdbcTemplate.query(sqlYear, new FilmRowMapper(namedParameterJdbcTemplate), directorId);
        } else {
            throw new RuntimeException("Некорректный запрос");
        }
        setFilmDirectors(directorFilms);
        return directorFilms;
    }

    private void addGenre(int filmId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }
        String deleteGenreSql = "DELETE FROM film_genres WHERE film_id = ?";
        String insertGenreSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        String placeholder = genres.stream().map(id -> "?").collect(Collectors.joining(","));
        String checkSql = String.format("Select genre_id FROM genres WHERE genre_id IN (%s)", placeholder);
        List<Integer> existingGenreIds = jdbcTemplate.queryForList(checkSql, genres.stream()
                .map(Genre::getId)
                .toArray(), Integer.class);
        if (existingGenreIds.size() != genres.size()) {
            Set<Integer> missingIds = genres.stream()
                    .map(Genre::getId)
                    .filter(id -> !existingGenreIds.contains(id))
                    .collect(Collectors.toSet());
            throw new NotFoundException("Жанры с id=" + missingIds + " не найдены в справочнике");
        }
        jdbcTemplate.update(deleteGenreSql, filmId);
        jdbcTemplate.batchUpdate(insertGenreSql, genres.stream()
                .map(genre -> new Object[]{filmId, genre.getId()})
                .collect(Collectors.toList()));
    }

    private void checkFilmPresence(int filmId) {
        String checkSql = "SELECT COUNT(*) FROM films WHERE id = ?";
        int counter = jdbcTemplate.queryForObject(checkSql, Integer.class, filmId);
        if (counter == 0) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);
        String sqlRequest = """
                SELECT f.*, m.*, COUNT(l.film_id) AS likes_count
                FROM films f
                JOIN mpa m ON f.mpa = m.mpa_id
                JOIN likes l ON f.id = l.film_id
                JOIN (
                    SELECT l1.film_id
                    FROM likes l1
                    JOIN likes l2 ON l1.film_id = l2.film_id
                    WHERE l1.user_id = :userId AND l2.user_id = :friendId
                ) common_likes ON f.id = common_likes.film_id
                GROUP BY f.id, m.mpa_id
                ORDER BY likes_count DESC
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);
        return namedParameterJdbcTemplate.query(sqlRequest, params, new FilmRowMapper(namedParameterJdbcTemplate));
    }

    @Override
    public List<Film> getSearchResults(String query, String by) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        StringBuilder sqlBuilder = new StringBuilder("""
                SELECT f.id, f.name, f.description, f.duration, f.release_date, mpa.*
                FROM films AS f
                JOIN mpa ON f.mpa = mpa.mpa_id
                LEFT JOIN likes AS l ON f.id = l.film_id
                """);
        boolean searchByDirector = by.contains("director");
        if (searchByDirector) {
            sqlBuilder.append("""
                    LEFT JOIN film_directors AS fd ON f.id = fd.film_id
                    LEFT JOIN directors AS d ON fd.director_id = d.director_id
                    """);
        }
        List<String> whereConditions = new ArrayList<>();
        if (by.contains("title")) {
            whereConditions.add("LOWER(f.name) LIKE LOWER(CONCAT('%',?,'%'))");
        }
        if (searchByDirector) {
            whereConditions.add("LOWER(d.name) LIKE LOWER(CONCAT('%',?,'%'))");
        }

        if (!whereConditions.isEmpty()) {
            sqlBuilder.append("WHERE ")
                    .append(String.join(" OR ", whereConditions));
        }
        sqlBuilder.append("""
                GROUP BY f.id
                ORDER BY COUNT(l.user_id) DESC
                """);
        List<Film> films;
        String sql = sqlBuilder.toString();

        if (by.equals("title,director") || by.equals("director,title")) {
            films = jdbcTemplate.query(sql, new FilmRowMapper(namedParameterJdbcTemplate), query, query);
        } else {
            films = jdbcTemplate.query(sql, new FilmRowMapper(namedParameterJdbcTemplate), query);
        }

        setFilmDirectors(films);
        return films;
    }

    public List<Film> getUserRecommendations(int userId) {
        userStorage.getUserById(userId);
        String sql = """
                SELECT f.*, m.*
                  FROM LIKES l1
                 INNER JOIN LIKES l2 ON l1.FILM_ID = l2.FILM_ID
                 INNER JOIN LIKES l3 ON l2.USER_ID = l3.USER_ID
                 INNER JOIN FILMS f ON l3.FILM_ID = f.ID
                 INNER JOIN MPA m ON f.MPA = m.MPA_ID
                 WHERE l1.USER_ID = ?
                   AND l3.FILM_ID NOT IN (SELECT l.FILM_ID FROM LIKES l WHERE l.USER_ID = l1.USER_ID )
                ORDER BY f.ID
                """;
        return jdbcTemplate.query(sql, new FilmRowMapper(namedParameterJdbcTemplate), userId);

    }

    private void addDirectors(int filmId, Set<Director> directors) {
        if (directors == null || directors.isEmpty()) {
            return;
        }
        directors.forEach(director -> {
            if (director.hasId()) {
                jdbcTemplate.update("DELETE FROM film_directors WHERE film_id = ? AND director_id = ?", filmId,
                        director.getId());
                String sql = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";
                jdbcTemplate.update(sql, filmId, director.getId());
                if (director.hasName()) {
                    jdbcTemplate.update("DELETE FROM directors WHERE director_id = ?", director.getId());
                    sql = "INSERT INTO directors (director_id, name) VALUES (?, ?)";
                    jdbcTemplate.update(sql, director.getId(), director.getName());
                }
            }
        });
    }

    private void setFilmDirectors(List<Film> films) {
        films.forEach(film -> {
            film.setDirectors(directorDbStorage.getFilmDirectors(film.getId()));
        });
    }

    private void setFilmDirectors(Film film) {
        if (Objects.nonNull(film)) {
            film.setDirectors(directorDbStorage.getFilmDirectors(film.getId()));
        }
    }
}
