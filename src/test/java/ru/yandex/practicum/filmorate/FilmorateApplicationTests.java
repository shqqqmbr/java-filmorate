package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, FilmDbStorage.class, MpaDbStorage.class})
class FilmoRateApplicationTests {
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private final MpaDbStorage mpaStorage;


    @BeforeEach
    void setUp() {
        User user = new User();
        user.setName("John");
        user.setLogin("Login");
        user.setBirthday(LocalDate.now().minusDays(40));
        user.setEmail("email@mail.ru");
        userStorage.addUser(user);

        Film film = new Film();
        film.setName("Film");
        film.setReleaseDate(LocalDate.now().minusDays(2));
        film.setDescription("Description");
        film.setDuration(155);
        film.setMpa(mpaStorage.getMpaById(1));
        filmStorage.addFilm(film);
    }

    @Test
    public void getUserByIdTest() {
        User user = userStorage.getUserById(1);
        assertThat(user.getId()).isEqualTo(1);
    }

    @Test
    public void getFilmByIdTest() {
        Film film = filmStorage.getFilmById(1);
        assertThat(film.getId()).isEqualTo(1);
    }
}
