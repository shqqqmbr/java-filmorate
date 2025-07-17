package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.ReviewRowMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.enums.EventTypes;
import ru.yandex.practicum.filmorate.model.enums.OperationTypes;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    @Autowired
    public ReviewDbStorage(JdbcTemplate jdbcTemplate, UserDbStorage userStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.userStorage = new UserDbStorage(jdbcTemplate);
        this.filmStorage = new FilmDbStorage(jdbcTemplate, userStorage);
    }

    @Override
    public Review addReview(Review review) {
        userStorage.getUserById(review.getUserId());
        filmStorage.getFilmById(review.getFilmId());

        String sql = "INSERT INTO REVIEWS (content, is_positive, user_id, film_id) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setInt(3, review.getUserId());
            ps.setInt(4, review.getFilmId());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            review.setReviewId(keyHolder.getKey().intValue());
        } else {
            throw new RuntimeException("Не удалось получить сгенерированный ID");
        }
        userStorage.addUserFeed(review.getReviewId(), review.getUserId(), EventTypes.REVIEW, OperationTypes.ADD);
        return review;
    }

    @Override
    public Review updateReview(Review newReview) {
        checkReviewPresence(newReview.getReviewId());
        userStorage.getUserById(newReview.getUserId());
        filmStorage.getFilmById(newReview.getFilmId());
        String sql = "UPDATE REVIEWS SET content=?, is_positive=? WHERE review_id=?";
        jdbcTemplate.update(
                sql,
                newReview.getContent(),
                newReview.getIsPositive(),
                newReview.getReviewId()
        );
        newReview.setUseful(calculateUseful(newReview.getReviewId()));
        userStorage.addUserFeed(newReview.getReviewId(), newReview.getUserId(), EventTypes.REVIEW, OperationTypes.UPDATE);
        return newReview;
    }

    @Override
    public void deleteReview(int id) {
        checkReviewPresence(id);
        Review review = getReviewById(id);
        String sql = "DELETE FROM REVIEWS WHERE review_id = ?";
        jdbcTemplate.update(sql, id);
        userStorage.addUserFeed(review.getReviewId(), review.getUserId(), EventTypes.REVIEW, OperationTypes.REMOVE);
    }

    @Override
    public Review getReviewById(int id) {
        checkReviewPresence(id);
        String sql = "SELECT * FROM REVIEWS WHERE review_id = ?";
        Review review = jdbcTemplate.queryForObject(sql, new ReviewRowMapper(), id);
        return review;
    }

    @Override
    public List<Review> getReviewsByFilmId(int filmId, int count) {
        String sql = "SELECT * FROM REVIEWS WHERE film_id = ? LIMIT ?";
        List<Review> reviews = jdbcTemplate.query(sql, new Object[]{filmId, count}, new ReviewRowMapper());
        for (Review review : reviews) {
            review.setUseful(calculateUseful(review.getReviewId()));
        }
        return reviews;
    }

    @Override
    public List<Review> getAllReviews(int count) {
        String sql = "SELECT r.*, COALESCE(SUM(rl.useful), 0) as useful_sum " +
                "FROM REVIEWS r LEFT JOIN review_likes rl ON r.review_id = rl.review_id " +
                "GROUP BY r.review_id " +
                "ORDER BY useful_sum DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(sql, new ReviewRowMapper(), count);
    }

    @Override
    public void addLike(int reviewId, int userId) {
        checkReviewPresence(reviewId);
        userStorage.getUserById(userId);

        String checkSql = "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, reviewId, userId);

        if (count == 0) {
            String insertSql = "INSERT INTO review_likes (review_id, user_id, useful) VALUES (?, ?, 1)";
            jdbcTemplate.update(insertSql, reviewId, userId);
        } else {
            String selectSql = "SELECT useful FROM review_likes WHERE review_id = ? AND user_id = ?";
            Integer currentUseful = jdbcTemplate.queryForObject(selectSql, Integer.class, reviewId, userId);
            if (currentUseful == -1) {
                String updateSql = "UPDATE review_likes SET useful = 1 WHERE review_id = ? AND user_id = ?";
                jdbcTemplate.update(updateSql, reviewId, userId);
            }
        }
    }

    @Override
    public void addDislike(int reviewId, int userId) {
        // Проверяем существование отзыва и пользователя
        checkReviewPresence(reviewId);
        userStorage.getUserById(userId);

        // Изменяем запрос и обработку результата
        String checkSql = "SELECT useful FROM review_likes WHERE review_id = ? AND user_id = ?";

        try {
            Integer currentUseful = jdbcTemplate.queryForObject(checkSql, Integer.class, reviewId, userId);

            if (currentUseful == 1) {
                String updateSql = "UPDATE review_likes SET useful = -1 WHERE review_id = ? AND user_id = ?";
                jdbcTemplate.update(updateSql, reviewId, userId);
            }
        } catch (EmptyResultDataAccessException e) {
            // Если записи нет - вставляем новую
            String insertSql = "INSERT INTO review_likes (review_id, user_id, useful) VALUES (?, ?, -1)";
            jdbcTemplate.update(insertSql, reviewId, userId);
        }
    }

    @Override
    public void deleteLike(int reviewId, int userId) {
        checkReviewPresence(reviewId);
        userStorage.getUserById(userId);

        String checkSql = "SELECT useful FROM review_likes WHERE review_id = ? AND user_id = ?";
        Integer currentUseful = jdbcTemplate.queryForObject(checkSql, Integer.class, reviewId, userId);

        if (currentUseful == 1) {
            String deleteSql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";
            jdbcTemplate.update(deleteSql, reviewId, userId);
        }
    }

    @Override
    public void deleteDislike(int reviewId, int userId) {
        checkReviewPresence(reviewId);
        userStorage.getUserById(userId);

        String checkSql = "SELECT useful FROM review_likes WHERE review_id = ? AND user_id = ?";
        Integer currentUseful = jdbcTemplate.queryForObject(checkSql, Integer.class, reviewId, userId);

        if (currentUseful == -1) {
            String deleteSql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";
            jdbcTemplate.update(deleteSql, reviewId, userId);
        }
    }

    private void checkReviewPresence(int reviewId) {
        String checkSql = "SELECT COUNT(*) FROM REVIEWS WHERE review_id = ?";
        int count = jdbcTemplate.queryForObject(checkSql, Integer.class, reviewId);
        if (count == 0) {
            throw new NotFoundException("Отзыв с id=" + reviewId + " не найден");
        }
    }

    private int calculateUseful(int reviewId) {
        String sql = "SELECT SUM(useful) FROM review_likes WHERE review_id = ?";
        Integer rating = jdbcTemplate.queryForObject(sql, Integer.class, reviewId);
        return rating != null ? rating : 0;
    }
}