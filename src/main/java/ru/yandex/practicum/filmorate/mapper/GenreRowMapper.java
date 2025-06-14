package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GenreRowMapper implements RowMapper<Genre> {
    @Override
    public Genre mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Genre genre = new Genre();
        genre.setGenreId(resultSet.getInt("genre_id"));
        genre.setGenreName(resultSet.getString("genre_name"));
        return genre;
    }
}
