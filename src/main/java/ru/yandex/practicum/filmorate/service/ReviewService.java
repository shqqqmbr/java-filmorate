package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage storage;

    public Review addReview(Review review) {
        return storage.addReview(review);
    }

    public Review updateReview(Review newReview) {
        return storage.updateReview(newReview);
    }

    public void deleteReview(int id) {
        storage.deleteReview(id);
    }

    public Review getReviewById(int id) {
        return storage.getReviewById(id);
    }

    public List<Review> getReviewsByFilmId(int filmId, int count) {
        return storage.getReviewsByFilmId(filmId, count);
    }

    public List<Review> getAllReviews(int count) {
        return storage.getAllReviews(count);
    }

    public void addLike(int reviewId, int userId) {
        storage.addLike(reviewId, userId);
    }

    public void addDislike(int reviewId, int userId) {
        storage.addDislike(reviewId, userId);
    }

    public void deleteLike(int reviewId, int userId) {
        storage.deleteLike(reviewId, userId);
    }

    public void deleteDislike(int reviewId, int userId) {
        storage.deleteDislike(reviewId, userId);
    }
}
