package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class
Director {
    @Positive
    @NotNull
    private Integer id;
    @NotBlank
    private String name;

    public boolean hasId() {
        return !(id == null);
    }

    public boolean hasName() {
        return !(name == null || name.isBlank());
    }
}
