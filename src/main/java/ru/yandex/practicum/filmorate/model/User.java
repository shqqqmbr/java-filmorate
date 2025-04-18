package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;

@Data
public class User {
    private int id;
    @NotBlank
    @Email(message = "Email должен содержать символ @")
    private String email;
    @NotBlank
    @Pattern(regexp = "\\S+", message = "Логин не может быть пустым и содержаать пробелы")
    private String login;

    private String name;
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    private final Logger log = LoggerFactory.getLogger(User.class);

    public void validation() {
        if (birthday.isAfter(LocalDate.now())) {
            log.warn("Дата рождения {} некорректна", birthday);
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    public String getName() {
        return name == null || name.isBlank() ? login : name;
    }
}
