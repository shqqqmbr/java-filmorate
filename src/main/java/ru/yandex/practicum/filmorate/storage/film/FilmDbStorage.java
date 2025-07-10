package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.mapper.IntegerRowMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaDbStorage mpaDbStorage;
    private final UserStorage userStorage;
    private final DirectorDbStorage directorDbStorage;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaDbStorage = new MpaDbStorage(jdbcTemplate);
        this.userStorage = new UserDbStorage(jdbcTemplate);
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
        addDirectors(newFilm.getId(), newFilm.getDirectors());
        newFilm.setGenres(newFilm.getGenres());
        return newFilm;
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration,"
                + "f.mpa AS mpa_id, m.mpa_name "
                + "FROM FILMS f "
                + "LEFT JOIN mpa m ON f.mpa = m.mpa_id";
        return jdbcTemplate.query(sql, new FilmRowMapper());
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
        Film film = jdbcTemplate.queryForObject(filmSql, new FilmRowMapper(), filmId);
        String genresSql = "SELECT g.genre_id, g.genre_name "
                + "FROM film_genres fg "
                + "JOIN genres g ON fg.genre_id = g.genre_id "
                + "WHERE fg.film_id = ? ORDER BY g.genre_id ASC";
        Set<Genre> genres = new HashSet<>(jdbcTemplate.query(genresSql,
                (rs, rowNum) -> new Genre(rs.getInt("genre_id"),
                        rs.getString("genre_name")),
                filmId
        ));
        film.setGenres(genres);
        String likesSql = "SELECT user_id FROM likes WHERE film_id = ?";
        Set<Integer> likes = new HashSet<>(jdbcTemplate.queryForList(likesSql, Integer.class, filmId));
        film.setLikes(likes);
        String directorsSql = "SELECT * FROM directors WHERE director_id IN (SELECT director_id "
                + "FROM film_directors "
                + "WHERE film_id = ?)";
        Set<Director> directors = new HashSet<>(jdbcTemplate.query(directorsSql,
                (rs, rowNum) -> new Director(rs.getInt("director_id"),
                        rs.getString("name")),
                filmId
        ));
        film.setDirectors(directors);
        return film;
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
        String sql = """
                SELECT films.*, mpa.*, COUNT(likes.film_id) AS likes_count
                FROM films
                JOIN mpa ON films.mpa = mpa.mpa_id
                LEFT JOIN likes ON films.id = likes.film_id
                GROUP BY films.id, mpa.mpa_id
                ORDER BY likes_count DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, new FilmRowMapper(), count);
    }

    @Override
    public List<Film> getSortedFilms(int directorId, String sortBy) {
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
            directorFilms = jdbcTemplate.query(sqlLikes, new FilmRowMapper(), directorId);
        } else if (sortBy.equalsIgnoreCase("year")) {
            directorFilms = jdbcTemplate.query(sqlYear, new FilmRowMapper(), directorId);
        } else {
            throw new RuntimeException("Некорректный запрос");
        }
        setFilmDirectors(directorFilms);
        return directorFilms;
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
        String sqlRequest = """
                SELECT l1.film_id
                FROM likes l1
                WHERE l1.user_id = ?
                INTERSECT
                SELECT l2.film_id
                FROM likes l2
                WHERE l2.user_id = ?
                """;

        // для проверки, существует ли пользователь
        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);

        // не стал использовать один запрос с выводом фильмов, т.к. после применения FilmRowMapper
        // нужно будет заполнять пустые коллекции т.е. дублировать код getFilmById,
        // решил использовать сортировку на уровне приложения
        return jdbcTemplate.query(sqlRequest, new IntegerRowMapper(), userId, friendId).stream()
                .map(this::getFilmById)
                .sorted((f1, f2) -> f2.getLikes().size() - f1.getLikes().size()) //т.к. порядок сортировки после map не сохраняется, использовать сортировку в запросе бесполезно
                .collect(Collectors.toList());
    }

    private void addDirectors(int filmId, Set<Director> directors) {
        if (directors == null || directors.isEmpty()) {
            return;
        }
        directors.forEach(director -> {
            if (director.hasId()) {
                String sql = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";
                jdbcTemplate.update(sql, filmId, director.getId());
            }
        });
    }

    private void setFilmDirectors(List<Film> films) {
        films.forEach(film -> {
            film.setDirectors(directorDbStorage.getFilmDirectors(film.getId()));
        });
    }
}
