package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import com.reactivespring.repository.ReviewReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ReviewHandler {

    Sinks.Many<Review> reviewsSink = Sinks.many().replay().latest();
    @Autowired
    private Validator validator;
    private ReviewReactiveRepository reviewReactiveRepository;

    public ReviewHandler(ReviewReactiveRepository reviewReactiveRepository) {
        this.reviewReactiveRepository = reviewReactiveRepository;
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {

        return request.bodyToMono(Review.class)
                .doOnNext(this::validate)
                .flatMap(reviewReactiveRepository::save)
                .doOnNext(review -> {
                    reviewsSink.tryEmitNext(review);
                })
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
    }

    private void validate(Review review) {

        var constraintViolations = validator.validate(review);
        log.info("ConstraintViolations: {}", constraintViolations);
        if (constraintViolations.size() > 0) {
            var errorMessage = constraintViolations
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(","));

            throw new ReviewDataException(errorMessage);
        }
    }

    public Mono<ServerResponse> getReviews(ServerRequest request) {

        var movieInfoId = request.queryParam("movieInfoId");

        if (movieInfoId.isPresent()) {
            return buildReviewsResponse(reviewReactiveRepository.findReviewByMovieInfoId(Long.valueOf(movieInfoId.get())));
        } else {
            return buildReviewsResponse(reviewReactiveRepository.findAll());
        }

    }

    public Mono<ServerResponse> buildReviewsResponse(Flux<Review> reviewFlux) {
        return ServerResponse.ok()
                .body(reviewFlux, Review.class);
    }

    public Mono<ServerResponse> updateReview(ServerRequest serverRequest) {

        var reviewId = serverRequest.pathVariable("id");
        var review = reviewReactiveRepository.findById(reviewId)
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not found for the given review id: " + reviewId)));//Better approach

        return review.flatMap(oldReview -> serverRequest.bodyToMono(Review.class)
                        .map(newReview -> {
                            oldReview.setMovieInfoId(newReview.getMovieInfoId());
                            oldReview.setRating(newReview.getRating());
                            oldReview.setComment(newReview.getComment());
                            return oldReview;
                        })
                        .flatMap(reviewReactiveRepository::save)
                        .flatMap(ServerResponse.ok()::bodyValue)
                //.switchIfEmpty(ServerResponse.notFound().build()) //Alternate approach
        );
    }

    public Mono<ServerResponse> deleteReview(ServerRequest serverRequest) {

        var reviewId = serverRequest.pathVariable("id");
        return reviewReactiveRepository.deleteById(reviewId)
                .then(ServerResponse.noContent().build());
    }
}
