package com.reactivespring;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 9000)
@TestPropertySource(
        properties = {
                "restClient.moviesInfoUrl = http://localhost:9000/v1/movieinfos",
                "restClient.reviewsUrl = http://localhost:9000/v1/reviews"
        }
)
public class MoviesControllerIntgTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void getMovieById() {

        var movieId = "1";

        stubFor(get(urlPathEqualTo("/v1/movieinfos" + "/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")));


        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    var movieResponse = movieEntityExchangeResult.getResponseBody();
                    assert Objects.requireNonNull(movieResponse).getReviewList().size() == (2);
                    assertEquals("Batman Begins", movieResponse.getMovieInfo().getName());
                });

    }

    @Test
    void getMovieById_reviews_404() {

        var movieId = "1";

        stubFor(get(urlPathEqualTo("/v1/movieinfos" + "/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withStatus(404)));

        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    var movieResponse = movieEntityExchangeResult.getResponseBody();
                    assert Objects.requireNonNull(movieResponse).getReviewList().size() == 0;
                    assertEquals("Batman Begins", movieResponse.getMovieInfo().getName());
                });

    }

    @Test
    void getMovieById_review_5XX() {

        var movieId = "1";

        stubFor(get(urlPathEqualTo("/v1/movieinfos" + "/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("service not available")));

        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server exception in ReviewsService service not available");

        WireMock.verify(3, getRequestedFor(urlPathMatching("/v1/reviews*")));
    }
}

