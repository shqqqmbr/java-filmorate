package ru.yandex.practicum.filmorate.loader;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ReviewDataLoader {
    private final JdbcTemplate jdbcTemplate;

    public int calculateUseful(int reviewId) {
        String sql = "SELECT SUM(useful) FROM review_likes WHERE review_id = ?";
        Integer rating = jdbcTemplate.queryForObject(sql, Integer.class, reviewId);
        return rating != null ? rating : 0;
    }
}
