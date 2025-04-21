package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.MaximumDate;

import java.time.LocalDate;

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
    @MaximumDate(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    public String getName() {
        return name == null || name.isBlank() ? login : name;
    }
}
