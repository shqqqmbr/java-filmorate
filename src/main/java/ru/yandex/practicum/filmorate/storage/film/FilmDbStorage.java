package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final MpaDbStorage mpaDbStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.mpaDbStorage = new MpaDbStorage(jdbcTemplate);
        this.userStorage = new UserDbStorage(jdbcTemplate);
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
        return film;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        checkFilmPresence(newFilm.getId());
        mpaDbStorage.getMpaById(newFilm.getMpa().getId());
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
        addGenre(newFilm.getId(), newFilm.getGenres());
        newFilm.setGenres(newFilm.getGenres());
        return newFilm;
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration,"
                + "f.mpa AS mpa_id, m.mpa_name "
                + "FROM FILMS f "
                + "LEFT JOIN mpa m ON f.mpa = m.mpa_id";
        return jdbcTemplate.query(sql, new FilmRowMapper(namedParameterJdbcTemplate));
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
        return jdbcTemplate.queryForObject(filmSql, new FilmRowMapper(namedParameterJdbcTemplate), filmId);
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
    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {
        String sql = """
                SELECT films.*, mpa.*, COUNT(likes.film_id) AS likes_count
                FROM films
                JOIN mpa ON films.mpa = mpa.mpa_id
                LEFT JOIN likes ON films.id = likes.film_id
                LEFT JOIN film_genres ON films.ID = film_genres.film_id
                LEFT JOIN genres ON film_genres.genre_id = genres.genre_id
                WHERE (:genreId IS NULL OR genres.genre_id = :genreId)
                AND (:year IS NULL OR YEAR(films.release_date) = :year)
                GROUP BY films.id, mpa.mpa_id
                ORDER BY likes_count DESC
                LIMIT :limit
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("genreId", genreId)
                .addValue("year", year)
                .addValue("limit", count);

        return namedParameterJdbcTemplate.query(sql, params, new FilmRowMapper(namedParameterJdbcTemplate));
    }

    //    В методе addGenre я решил не использовать getGenreById. Избавился от конструкции
//            (+ ... +) путем добавления плейсхолдера.
    private void addGenre(int filmId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }
        checkFilmPresence(filmId);
        Set<Integer> genreIds = genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());
        String placeholder = genres.stream().map(id -> "?").collect(Collectors.joining(","));
        String checkSql = String.format("Select genre_id FROM genres WHERE genre_id IN (%s)", placeholder);
        List<Integer> existingGenreIds = jdbcTemplate.queryForList(checkSql, genreIds.toArray(), Integer.class);

        if (existingGenreIds.size() != genreIds.size()) {
            Set<Integer> missingIds = genreIds.stream()
                    .filter(id -> !existingGenreIds.contains(id))
                    .collect(Collectors.toSet());
            throw new NotFoundException("Жанры с id=" + missingIds + " не найдены в справочнике");
        }

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", filmId);
        String insertSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        jdbcTemplate.batchUpdate(insertSql, genres.stream()
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
        // для проверки, существует ли пользователь
        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);

        String sqlRequest = """
                SELECT f.*, m.*, COUNT(l.film_id) AS likes_count
                FROM films f
                JOIN mpa m ON f.mpa = m.mpa_id
                JOIN likes l ON f.id = l.film_id
                WHERE f.id IN (
                    SELECT l1.film_id
                    FROM likes l1
                    WHERE l1.user_id = :userId
                    INTERSECT
                    SELECT l2.film_id
                    FROM likes l2
                    WHERE l2.user_id = :friendId
                )
                GROUP BY f.id, m.mpa_id
                ORDER BY likes_count DESC
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);

        return namedParameterJdbcTemplate.query(sqlRequest, params, new FilmRowMapper(namedParameterJdbcTemplate));
    }
}
