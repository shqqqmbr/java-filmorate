package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ReviewRowMapper implements RowMapper<Review> {
    @Override
    public Review mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Review review = new Review();
        review.setReviewId(resultSet.getInt("review_id"));
        review.setContent(resultSet.getString("content"));
        review.setIsPositive(resultSet.getBoolean("is_positive"));
        review.setUserId(resultSet.getInt("user_id"));
        review.setFilmId(resultSet.getInt("film_id"));
        return review;
    }
}