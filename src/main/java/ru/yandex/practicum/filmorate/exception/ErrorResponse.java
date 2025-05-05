package ru.yandex.practicum.filmorate.exception;

import lombok.Getter;

@Getter
public class ErrorResponse {
    private String error;
    private String errorMessage;

    public ErrorResponse(String error, String errorMessage) {
        this.error = error;
        this.errorMessage = errorMessage;
    }
}
