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

    void addLike(int ReviewId, int userId);

    void addDislike(int ReviewId, int userId);

    void deleteLike(int ReviewId, int userId);

    void deleteDislike(int ReviewId, int userId);

}