package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, FilmDbStorage.class, MpaDbStorage.class})
class FilmorateApplicationTests {
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
    public void addUserTest() {
        User user2 = new User();
        user2.setName("Anna");
        user2.setLogin("Login");
        user2.setBirthday(LocalDate.now().minusDays(28));
        user2.setEmail("mail@mail.ru");
        userStorage.addUser(user2);
        List<User> users = userStorage.getAllUsers();
        Assertions.assertTrue(users.contains(user2));
    }

    @Test
    public void updateUserTest() {
        User newUser = new User();
        newUser.setName("Addie");
        newUser.setLogin("Login");
        newUser.setBirthday(LocalDate.now().minusDays(20));
        newUser.setEmail("newmail@mail.ru");
        newUser.setId(1);
        userStorage.updateUser(newUser);
        Assertions.assertTrue(userStorage.getUserById(1).getName().equals("Addie"));
    }

    @Test
    public void deleteUserTest() {
        userStorage.deleteUser(1);
        List<User> users = userStorage.getAllUsers();
        Assertions.assertTrue(users.isEmpty());
    }

    @Test
    public void getAllUsersTest() {
        User user2 = new User();
        user2.setName("Lana");
        user2.setLogin("Login");
        user2.setBirthday(LocalDate.now().minusDays(20));
        user2.setEmail("newmail@mail.ru");
        userStorage.addUser(user2);
        List<User> users = userStorage.getAllUsers();
        Assertions.assertTrue(users.size() == 2);
    }

    @Test
    public void getCommonFriendsTest() {
        User user1 = userStorage.getUserById(1);
        User user2 = new User();
        user2.setName("Sam");
        user2.setLogin("Login");
        user2.setBirthday(LocalDate.now().minusDays(20));
        user2.setEmail("newmail@mail.ru");
        userStorage.addUser(user2);

        User friend1 = new User();
        friend1.setName("friend1");
        friend1.setLogin("Login1");
        friend1.setBirthday(LocalDate.now().minusDays(18));
        friend1.setEmail("friend1@mail.ru");
        userStorage.addUser(friend1);
        User friend2 = new User();
        friend2.setName("friend2");
        friend2.setLogin("Login1");
        friend2.setBirthday(LocalDate.now().minusDays(18));
        friend2.setEmail("friend2@mail.ru");
        userStorage.addUser(friend2);

        userStorage.addFriend(1, 3);
        userStorage.addFriend(1, 4);
        userStorage.addFriend(2, 3);
        userStorage.addFriend(2, 4);
        List<User> commonFriends = userStorage.getCommonFriends(1, 2);
        Assertions.assertFalse(commonFriends.isEmpty());
        Assertions.assertTrue(commonFriends.size() == 2);
    }

    @Test
    public void addFriendTest() {
        User friend = new User();
        friend.setName("friend1");
        friend.setLogin("Login1");
        friend.setBirthday(LocalDate.now().minusDays(18));
        friend.setEmail("friend1@mail.ru");
        userStorage.addUser(friend);
        userStorage.addFriend(1, 2);
        List<User> friends = userStorage.getAllFriends(1);
        Assertions.assertTrue(friends.size() == 1);
    }

    @Test
    public void deleteFriendTest() {
        User friend = new User();
        friend.setName("friend1");
        friend.setLogin("Login1");
        friend.setBirthday(LocalDate.now().minusDays(18));
        friend.setEmail("friend1@mail.ru");
        userStorage.addUser(friend);
        userStorage.addFriend(1, 2);
        userStorage.deleteFriend(1, 2);
        List<User> friends = userStorage.getAllFriends(1);
        Assertions.assertTrue(friends.isEmpty());
    }

    @Test
    public void getAllFriendsTest() {
        User friend = new User();
        friend.setName("friend1");
        friend.setLogin("Login1");
        friend.setBirthday(LocalDate.now().minusDays(18));
        friend.setEmail("friend1@mail.ru");
        userStorage.addUser(friend);
        userStorage.addFriend(1, 2);
        List<User> friends = userStorage.getAllFriends(1);
        Assertions.assertTrue(friends.size() == 1);
    }

    @Test
    public void isFriendTest() {
        User friend = new User();
        friend.setName("friend1");
        friend.setLogin("Login1");
        friend.setBirthday(LocalDate.now().minusDays(18));
        friend.setEmail("friend1@mail.ru");
        userStorage.addUser(friend);
        userStorage.addFriend(1, 2);
        Assertions.assertTrue(userStorage.isFriend(1, 2));
    }

    @Test
    public void getFilmByIdTest() {
        Film film = filmStorage.getFilmById(1);
        assertThat(film.getId()).isEqualTo(1);
    }

    @Test
    public void addFilmTest() {
        Film film1 = new Film();
        film1.setName("Film");
        film1.setReleaseDate(LocalDate.now().minusDays(20));
        film1.setDescription("Description");
        film1.setDuration(200);
        film1.setMpa(mpaStorage.getMpaById(3));
        filmStorage.addFilm(film1);
        List<Film> films = filmStorage.getAllFilms();
        Assertions.assertTrue(films.contains(film1));
    }

    @Test
    public void updateFilmTest() {
        Film newFilm = new Film();
        newFilm.setName("newFilm");
        newFilm.setReleaseDate(LocalDate.now().minusDays(4));
        newFilm.setDescription("Description");
        newFilm.setDuration(300);
        newFilm.setMpa(mpaStorage.getMpaById(2));
        newFilm.setId(1);
        filmStorage.updateFilm(newFilm);
        Assertions.assertTrue(filmStorage.getFilmById(1).getName().equals("newFilm"));
    }

    @Test
    public void getAllFilmsTest() {
        List<Film> films = filmStorage.getAllFilms();
        Assertions.assertTrue(films.size() == 1);
    }

    @Test
    public void deleteFilmTest() {
        filmStorage.deleteFilm(1);
        List<Film> films = filmStorage.getAllFilms();
        Assertions.assertTrue(films.isEmpty());
    }

    @Test
    public void addLikeTest() {
        filmStorage.addLike(1, 1);
        Set<Integer> likes = filmStorage.getFilmById(1).getLikes();
        Assertions.assertTrue(likes.contains(1));
    }

    @Test
    public void deleteLikeTest() {
        filmStorage.addLike(1, 1);
        filmStorage.deleteLike(1, 1);
        Set<Integer> likes = filmStorage.getFilmById(1).getLikes();
        Assertions.assertTrue(likes.isEmpty());
    }

    @Test
    public void getPopularFilmsTest() {
        Film film1 = filmStorage.getFilmById(1);
        Film film2 = new Film();
        film2.setName("Film2");
        film2.setReleaseDate(LocalDate.now().minusDays(10));
        film2.setDescription("Description2");
        film2.setDuration(200);
        film2.setMpa(mpaStorage.getMpaById(1));
        filmStorage.addFilm(film2);
        Film film3 = new Film();
        film3.setName("Film");
        film3.setReleaseDate(LocalDate.now().minusDays(20));
        film3.setDescription("Description");
        film3.setDuration(200);
        film3.setMpa(mpaStorage.getMpaById(3));
        filmStorage.addFilm(film3);

        User user1 = new User();
        user1.setName("user1");
        user1.setLogin("Login1");
        user1.setBirthday(LocalDate.now().minusDays(18));
        user1.setEmail("user1@mail.ru");
        userStorage.addUser(user1);
        User user2 = new User();
        user2.setName("user2");
        user2.setLogin("Login2");
        user2.setBirthday(LocalDate.now().minusDays(18));
        user2.setEmail("user2@mail.ru");
        userStorage.addUser(user1);
        User user3 = new User();
        user3.setName("user3");
        user3.setLogin("Login3");
        user3.setBirthday(LocalDate.now().minusDays(18));
        user3.setEmail("user3@mail.ru");
        userStorage.addUser(user1);

        filmStorage.addLike(1, 1);
        filmStorage.addLike(1, 2);
        filmStorage.addLike(1, 3);
        filmStorage.addLike(1, 4);

        filmStorage.addLike(2, 1);
        filmStorage.addLike(2, 2);
        filmStorage.addLike(2, 3);

        filmStorage.addLike(3, 1);

        List<Film> films = filmStorage.getPopularFilms(2);
        Assertions.assertTrue(films.contains(film1));
        Assertions.assertTrue(films.contains(film2));
    }
}
