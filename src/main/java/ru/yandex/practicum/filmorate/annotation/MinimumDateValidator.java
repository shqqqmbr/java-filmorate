package ru.yandex.practicum.filmorate.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MinimumDateValidator implements ConstraintValidator<MinimumDate, LocalDate> {
    private LocalDate minDate;

    @Override
    public void initialize(MinimumDate constraintAnnotation) {
        this.minDate = LocalDate.parse(
                constraintAnnotation.value(),
                DateTimeFormatter.ISO_LOCAL_DATE
        );
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return !value.isBefore(minDate);
    }
}