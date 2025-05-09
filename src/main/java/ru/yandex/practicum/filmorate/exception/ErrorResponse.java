package ru.yandex.practicum.filmorate.exception;

import lombok.Getter;

@Getter
public class ErrorResponse {
    private final String error;
    private final String errorMessage;

    public ErrorResponse(String error, String errorMessage) {
        this.error = error;
        this.errorMessage = errorMessage;
    }
}
