package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class ReviewsIntgTest {

    @Autowired
    ReviewReactiveRepository reviewReactiveRepository;

    @Autowired
    WebTestClient webTestClient;

    public static String REVIEW_URL = "/v1/reviews";

    @BeforeEach
    void setUp() {
        var reviewsList = List.of(
                new Review("1001", 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0));
        reviewReactiveRepository.saveAll(reviewsList)
                .blockLast();
    }

    @AfterEach
    void tearDown() {
        reviewReactiveRepository.deleteAll().block();
    }

    @Test
    void addReview() {

        //given
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

        //when
        webTestClient.post()
                .uri(REVIEW_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedReview = movieInfoEntityExchangeResult.getResponseBody();
                    assert savedReview != null;
                    assert savedReview.getReviewId() != null;
                });
    }

    @Test
    void getReviews() {

        //when
        webTestClient.get()
                .uri(REVIEW_URL)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Review.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedReview = movieInfoEntityExchangeResult.getResponseBody().get(0);
                    assert savedReview != null;
                    assertEquals("Awesome Movie", savedReview.getComment());
                });
    }

    @Test
    void updateReview() {
        var newReview = new Review("1001", 2L, "Very Awesome Movie", 10.0);

        //when
        webTestClient.put()
                .uri(REVIEW_URL + "/{id}", 1001)
                .bodyValue(newReview)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Review.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedReview = movieInfoEntityExchangeResult.getResponseBody();
                    assert savedReview != null;
                    assertEquals("Very Awesome Movie", savedReview.getComment());
                });
    }

    @Test
    void deleteReview() {
        webTestClient.delete()
                .uri(REVIEW_URL + "/{id}", 1001)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void getReviewsByMovieInfoId() {

        var uriBuilder = UriComponentsBuilder.fromUri(URI.create(REVIEW_URL))
                .queryParam("movieInfoId", 1L)
                .build()
                .toUri();

        webTestClient.get()
                .uri(uriBuilder)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Review.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var reviewList = movieInfoEntityExchangeResult.getResponseBody();
                    assert reviewList != null;
                    assertEquals(2, reviewList.size());
                });
    }

    @Test
    void updateReview_validation() {

        var newReview = new Review("1002", 2L, "Very Awesome Movie", 10.0);

        //when
        webTestClient.put()
                .uri(REVIEW_URL + "/{id}", 1002)
                .bodyValue(newReview)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(String.class)
                .isEqualTo("Review not found for the given review id: 1002");
    }
}
