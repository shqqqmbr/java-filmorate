package ru.yandex.practicum.filmorate.storage.film;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int idCounter = 0;

    @Override
    public Film addFilm(Film film) {
        film.setId(++idCounter);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        if (!films.containsKey(newFilm.getId())) {
            throw new NotFoundException("Фильм с указанным id не существует");
        }
        films.put(newFilm.getId(), newFilm);
        return newFilm;
    }

    @Override
    public List<Film> getAllFilms() {
        List<Film> allFilms = new ArrayList<>(films.values());
        return allFilms;
    }

    @Override
    public void deleteFilm(int id) {
        if (!films.containsKey(id)){
            throw new NotFoundException("Фильм с данным ID не найден");
        }
        films.remove(id);
    }

    @Override
    public Film getFilmById(int id) {
        if (!films.containsKey(id)){
            throw new NotFoundException("Фильм с данным ID не найден");
        }
        return films.get(id);
    }
}
