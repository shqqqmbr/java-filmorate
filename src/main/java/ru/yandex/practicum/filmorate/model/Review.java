package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private int reviewId;
    @NotNull
    @JsonProperty(value = "content", required = true)
    private String content;
    @NotNull
    @JsonProperty(value = "isPositive", required = true)
    private Boolean isPositive;
    @NotNull
    @JsonProperty(value = "userId", required = true)
    private Integer userId;
    @NotNull
    @JsonProperty(value = "filmId", required = true)
    private Integer filmId;
    private int useful;
}