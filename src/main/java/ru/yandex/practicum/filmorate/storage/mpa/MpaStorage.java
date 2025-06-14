package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

public interface MpaStorage {
    Mpa getMpaById(int id);

    List<Mpa> getAllMpas();

    void deleteAllMpas();
}
