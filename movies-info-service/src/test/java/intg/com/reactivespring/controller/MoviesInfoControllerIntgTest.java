package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
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
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class MoviesInfoControllerIntgTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @Autowired
    WebTestClient webTestClient;

    static String MOVIE_INFOS_URL = "/v1/movieinfos";
    static String MOVIE_INFOS_ID = "123";
    static String MOVIE_INFOS_INVALID_ID = "456";

    @BeforeEach
    void beforeAll() {

        var data = List.of(new MovieInfo(null, "Devdas", 2004, List.of("Dev", "Paaro"), LocalDate.parse("2003-03-18")),
                new MovieInfo(null, "Transformers", 2003, List.of("Bumblebee", "Optimus Prime"), LocalDate.parse("2003-04-18")),
                new MovieInfo("123", "Tails of Dogesh", 2003, List.of("Dogesh", "Dogelina"), LocalDate.parse("2003-05-18")));

        movieInfoRepository.saveAll(data).blockLast();
    }

    @AfterEach
    void teardown() {

        movieInfoRepository.deleteAll().block();
    }

    @Test
    void addMovieInfos() {

        var movieInfo = new MovieInfo(null, "Devdas1", 2003, List.of("Dev", "Paaro"), LocalDate.parse("2003-03-18"));

        webTestClient.post()
                .uri(MOVIE_INFOS_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var result = movieInfoEntityExchangeResult.getResponseBody();
                    assert result != null;
                    assert result.getMovieInfoId() != null;
                });

    }

    @Test
    void getAllMovieInfos() {

        webTestClient.get()
                .uri(MOVIE_INFOS_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfoById() {
        webTestClient.get()
                .uri(MOVIE_INFOS_URL + "/{id}", MOVIE_INFOS_ID)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody() //another way
                .jsonPath("$.name", "Tails of Dogesh")
                /*.expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var result = movieInfoEntityExchangeResult.getResponseBody();
                    assert result!=null;
                    assertEquals("123", result.getMovieInfoId());
                })*/;
    }

    @Test
    void getMovieInfoByYear() {

        var uriComponents = UriComponentsBuilder.fromUri(URI.create(MOVIE_INFOS_URL))
                .queryParam("year", "2003")
                .build()
                .toUri();

        webTestClient.get()
                .uri(uriComponents)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(2);
    }

    @Test
    void getMovieInfoByName() {

        var uriComponents = UriComponentsBuilder.fromUri(URI.create(MOVIE_INFOS_URL))
                .queryParam("name", "Transformers")
                .build()
                .toUri();

        webTestClient.get()
                .uri(uriComponents)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);
    }

    @Test
    void updateMovieInfoById() {

        var movieInfo = new MovieInfo("123", "Devdas Again", 2015, List.of("Christian Balle Balle", "Michael Sugarcane"), LocalDate.parse("2003-03-18"));

        webTestClient.put()
                .uri(MOVIE_INFOS_URL + "/{id}", MOVIE_INFOS_ID)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var result = movieInfoEntityExchangeResult.getResponseBody();
                    assert result != null;
                    assertEquals("Devdas Again", result.getName());
                    assertEquals(2015, result.getYear());
                    assertEquals("Christian Balle Balle", result.getCast().get(0));
                });

    }

    @Test
    void updateMovieInfoById_notFound() {

        var movieInfo = new MovieInfo("123", "Devdas Again", 2015, List.of("Christian Balle Balle", "Michael Sugarcane"), LocalDate.parse("2003-03-18"));

        webTestClient.put()
                .uri(MOVIE_INFOS_URL + "/{id}", MOVIE_INFOS_INVALID_ID)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isNotFound();

    }

    @Test
    void getMovieInfoById_notFound() {
        webTestClient.get()
                .uri(MOVIE_INFOS_URL + "/{id}", MOVIE_INFOS_INVALID_ID)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void deleteMovieInfoById() {

        webTestClient.delete()
                .uri(MOVIE_INFOS_URL + "/{id}", MOVIE_INFOS_ID)
                .exchange()
                .expectStatus()
                .isNoContent()
                .expectBody(Void.class);
    }
}