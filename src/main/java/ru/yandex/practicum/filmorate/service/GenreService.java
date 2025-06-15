package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreStorage storage;

    public Genre getGenreById(int id) {
        return storage.getGenreById(id);
    }

    public List<Genre> getAllGenres() {
        return storage.getAllGenres();
    }
}
