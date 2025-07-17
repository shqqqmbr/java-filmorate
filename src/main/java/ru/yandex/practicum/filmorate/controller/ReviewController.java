package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService service;

    @PostMapping
    public Review add(@Valid @RequestBody Review review) {
        return service.addReview(review);
    }

    @PutMapping
    public Review update(@RequestBody Review newReview) {
        return service.updateReview(newReview);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        service.deleteReview(id);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable int id) {
        return service.getReviewById(id);
    }

    @GetMapping
    public List<Review> getReviews(
            @RequestParam(required = false) Integer filmId,
            @RequestParam(required = false, defaultValue = "10") Integer count
    ) {
        if (filmId != null) {
            return service.getReviewsByFilmId(filmId, count);
        } else {
            return service.getAllReviews(count);
        }
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable("id") int id, @PathVariable("userId") int userId) {
        service.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable("id") int id, @PathVariable("userId") int userId) {
        service.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable("id") int id, @PathVariable("userId") int userId) {
        service.deleteLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable("id") int id, @PathVariable("userId") int userId) {
        service.deleteDislike(id, userId);
    }
}