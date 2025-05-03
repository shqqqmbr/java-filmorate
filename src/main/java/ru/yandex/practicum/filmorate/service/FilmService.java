package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.List;

@Service
public class FilmService {
    public FilmStorage storage;
    public UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage storage, UserStorage userStorage) {
        this.storage = storage;
        this.userStorage = userStorage;
    }

    public Film addFilm(Film film) {
        return storage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        return storage.updateFilm(film);
    }

    public void deleteFilm(int id) {
        storage.deleteFilm(id);
    }

    public List<Film> getAllFilms() {
        return storage.getAllFilms();
    }

    public Film getFilmById(int id) {
        return storage.getFilmById(id);
    }

    public void putLike(int id, int userId) {
        Film film = storage.getFilmById(id);
        if (film.getLikes().contains(userId)) {
            throw new ValidationException("Пользователь " + userId + " уже лайкнул фильм " + id);
        }
        if (userStorage.getUserById(userId) == null) {
            throw new NotFoundException("Пользователь с данным ID найден");
        }
        film.getLikes().add(userId);
        storage.updateFilm(film);
    }

    public void deleteLike(int id, int userId) {
        Film film = storage.getFilmById(id);
        if (userStorage.getUserById(userId) == null) {
            throw new NotFoundException("Пользователь с данным ID не найден");
        }
        film.getLikes().remove(userId);
        storage.updateFilm(film);
    }

    public List<Film> getPopularFilms(int count) {
        List<Film> films = new ArrayList<>(storage.getAllFilms());
        films.sort((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()));
        return films.stream().limit(count).toList();
    }
}
