package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    Film addFilm(Film film);

    Film updateFilm(Film newFilm);

    List<Film> getAllFilms();

    void deleteFilm(int id);

    Film getFilmById(int id);

}
