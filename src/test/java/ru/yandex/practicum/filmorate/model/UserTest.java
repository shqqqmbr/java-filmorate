package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Set;

@SpringBootTest
public class UserTest {
    private Validator validator;
    private User user;

    @BeforeEach
    public void beforeEach() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        user = new User();
        user.setName("Name");
        user.setId(1);
        user.setLogin("login");
        user.setEmail("maksim@mail.ru");
        user.setBirthday(LocalDate.of(2006, 11, 16));
    }

    @Test
    public void birthdayNotInFuture() {
        user.setBirthday(LocalDate.of(2026, 11, 16));
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        Assertions.assertFalse(violations.isEmpty());
        Assertions.assertEquals("Дата рождения не может быть в будущем",
                violations.iterator().next().getMessage());
    }

    @Test
    public void emailIsNotEmpty() {
        user.setEmail("");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        Assertions.assertFalse(violations.isEmpty());
        user.setEmail("mail.mail.ru");
        Set<ConstraintViolation<User>> violations1 = validator.validate(user);
        Assertions.assertFalse(violations1.isEmpty());
        Assertions.assertEquals("Email должен содержать символ @",
                violations1.iterator().next().getMessage());
    }

    @Test
    public void loginIsNotEmptyAndHaveNotSpaces() {
        user.setLogin("");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        Assertions.assertFalse(violations.isEmpty());
        user.setLogin("Login Login");
        Set<ConstraintViolation<User>> violations1 = validator.validate(user);
        Assertions.assertFalse(violations1.isEmpty());
        Assertions.assertEquals("Логин не может быть пустым и содержать пробелы",
                violations1.iterator().next().getMessage());
    }
}
