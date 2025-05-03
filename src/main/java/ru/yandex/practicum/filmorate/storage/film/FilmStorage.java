package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    public Film addFilm(Film film);

    public Film updateFilm(Film newFilm);

    public List<Film> getAllFilms();

    public void deleteFilm(int id);

    public Film getFilmById(int id);

}
