package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private int id;
    @NotBlank
    @Email(message = "Email должен содержать символ @")
    private String email;
    @NotBlank
    @Pattern(regexp = "\\S+", message = "Логин не может быть пустым и содержать пробелы")
    private String login;
    private String name;
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
    private Set<Integer> friends = new HashSet<>();

    public String getName() {
        return name == null || name.isBlank() ? login : name;
    }

    public void setName(String name) {
        this.name = (name == null || name.isBlank()) ? this.login : name;
    }

    public void addFriend(int friendId){
        friends.add(friendId);
    }

    public void deleteFriend(int friendId){
        friends.remove(friendId);
    }
}
