package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaStorage storage;

    public Mpa getMpaById(int id) {
        return storage.getMpaById(id);
    }

    public List<Mpa> getAllMpas() {
        return storage.getAllMpas();
    }

    public void deleteAllMpas() {
        storage.deleteAllMpas();
    }
}
