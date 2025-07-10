package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage storage;

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

    public void addLike(int id, int userId) {
        storage.addLike(id, userId);
    }

    public void deleteLike(int id, int userId) {
        storage.deleteLike(id, userId);
    }

    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {
        return storage.getPopularFilms(count, genreId, year);
    }

    public List<Film> getCommonFilms(int userId, int friendId) {
        return storage.getCommonFilms(userId, friendId);
    }

    public List<Film> getSortedFilms(int directorId, String sortBy) {
        return storage.getSortedFilms(directorId, sortBy);
    }
}