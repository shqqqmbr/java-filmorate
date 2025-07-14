package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.controller.marker.Marker;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Director {
    @Positive(groups = Marker.OnUpdate.class)
    @NotNull(groups = Marker.OnUpdate.class)
    private Integer id;
    @NotBlank(groups = Marker.OnCreate.class)
    private String name;

    public boolean hasId() {
        return !(id == null);
    }

    public boolean hasName() {
        return !(name == null || name.isBlank());
    }
}
