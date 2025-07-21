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
import java.util.Comparator;
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
        Review updatedReview = getReviewById(newReview.getReviewId());
        updatedReview.setContent(newReview.getContent());
        updatedReview.setIsPositive(newReview.getIsPositive());
        userStorage.getUserById(newReview.getUserId());
        filmStorage.getFilmById(newReview.getFilmId());
        String sql = "UPDATE REVIEWS SET content=?, is_positive=? WHERE review_id=?";
        jdbcTemplate.update(
                sql,
                updatedReview.getContent(),
                updatedReview.getIsPositive(),
                updatedReview.getReviewId()
        );
        userStorage.addUserFeed(updatedReview.getReviewId(), updatedReview.getUserId(), EventTypes.REVIEW, OperationTypes.UPDATE);
        return updatedReview;
    }

    @Override
    public void deleteReview(int id) {
        try {
            Review review = getReviewById(id);
            String sql = "DELETE FROM REVIEWS WHERE review_id = ?";
            jdbcTemplate.update(sql, id);
            userStorage.addUserFeed(review.getReviewId(), review.getUserId(), EventTypes.REVIEW, OperationTypes.REMOVE);
        } catch (Exception e) {
            throw new NotFoundException("Отзыв с id=" + id + " не найден");
        }
    }

    @Override
    public Review getReviewById(int id) {
        try {
            String sql = "SELECT * FROM REVIEWS WHERE review_id = ?";
            Review review = jdbcTemplate.queryForObject(sql, new ReviewRowMapper(jdbcTemplate), id);
            return review;
        } catch (Exception e) {
            throw new NotFoundException("Отзыв с id=" + id + " не найден");
        }
    }

    @Override
    public List<Review> getReviewsByFilmId(int filmId, int count) {
        String sql = "SELECT * FROM REVIEWS WHERE film_id = ? LIMIT ?";
        List<Review> reviews = jdbcTemplate.query(sql, new Object[]{filmId, count}, new ReviewRowMapper(jdbcTemplate));

        return reviews.stream()
                .sorted(Comparator.comparing(Review::getUseful).reversed())
                .toList();
    }

    @Override
    public List<Review> getAllReviews(int count) {
        String sql = """
                SELECT r.*, COALESCE(like_sum, 0) as useful_sum
                FROM REVIEWS r
                LEFT JOIN (
                    SELECT review_id, SUM(useful) as like_sum
                    FROM review_likes
                    GROUP BY review_id
                ) rl ON r.review_id = rl.review_id
                ORDER BY useful_sum DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, new ReviewRowMapper(jdbcTemplate), count);
    }

    @Override
    public void addLike(int reviewId, int userId) {
        try {
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
        } catch (Exception e) {
            throw new NotFoundException("Отзыв с id=" + reviewId + " не найден");
        }
    }

    @Override
    public void addDislike(int reviewId, int userId) {
        try {
            userStorage.getUserById(userId);
            String checkSql = "SELECT useful FROM review_likes WHERE review_id = ? AND user_id = ?";
            try {
                Integer currentUseful = jdbcTemplate.queryForObject(checkSql, Integer.class, reviewId, userId);
                if (currentUseful == 1) {
                    String updateSql = "UPDATE review_likes SET useful = -1 WHERE review_id = ? AND user_id = ?";
                    jdbcTemplate.update(updateSql, reviewId, userId);
                }
            } catch (EmptyResultDataAccessException e) {
                String insertSql = "INSERT INTO review_likes (review_id, user_id, useful) VALUES (?, ?, -1)";
                jdbcTemplate.update(insertSql, reviewId, userId);
            }
        } catch (Exception e) {
            throw new NotFoundException("Отзыв с id=" + reviewId + " не найден");
        }
    }

    @Override
    public void deleteLike(int reviewId, int userId) {
        try {
            userStorage.getUserById(userId);
            String checkSql = "SELECT useful FROM review_likes WHERE review_id = ? AND user_id = ?";
            Integer currentUseful = jdbcTemplate.queryForObject(checkSql, Integer.class, reviewId, userId);
            if (currentUseful == 1) {
                String deleteSql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";
                jdbcTemplate.update(deleteSql, reviewId, userId);
            }
        } catch (Exception e) {
            throw new NotFoundException("Отзыв с id=" + reviewId + " не найден");
        }
    }

    @Override
    public void deleteDislike(int reviewId, int userId) {
        try {
            userStorage.getUserById(userId);
            String checkSql = "SELECT useful FROM review_likes WHERE review_id = ? AND user_id = ?";
            Integer currentUseful = jdbcTemplate.queryForObject(checkSql, Integer.class, reviewId, userId);
            if (currentUseful == -1) {
                String deleteSql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";
                jdbcTemplate.update(deleteSql, reviewId, userId);
            }
        } catch (Exception e) {
            throw new NotFoundException("Отзыв с id=" + reviewId + " не найден");
        }
    }
}