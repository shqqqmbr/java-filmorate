package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.springframework.data.relational.core.mapping.Column;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mpa {
    @Positive
    @Column("mpa_id")
    private int id;
    @NotBlank
    @Column("mpa_name")
    private String name;
}
