package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    Review addReview(Review review);

    Review updateReview(Review newReview);

    void deleteReview(int id);

    Review getReviewById(int id);

    List<Review> getReviewsByFilmId(int filmId, int count);

    List<Review> getAllReviews(int count);

    void addLike(int reviewId, int userId);

    void addDislike(int reviewId, int userId);

    void deleteLike(int reviewId, int userId);

    void deleteDislike(int reviewId, int userId);

}
