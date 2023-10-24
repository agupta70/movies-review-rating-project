package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MoviesInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = MoviesInfoController.class)
@AutoConfigureWebTestClient
class MoviesInfoControllerUnitTest {

    static String MOVIE_INFOS_URL = "/v1/movieinfos";
    static String MOVIE_INFOS_ID = "123";

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    MoviesInfoService moviesInfoServiceBean;

    @Test
    public void getAllMoviesInfo() {
        var data = List.of(new MovieInfo(null, "Devdas", 2003, List.of("Dev", "Paaro"), LocalDate.parse("2003-03-18")),
                new MovieInfo(null, "Transformers", 2003, List.of("Bumblebee", "Optimus Prime"), LocalDate.parse("2003-04-18")),
                new MovieInfo("123", "Tails of Dogesh", 2003, List.of("Dogesh", "Dogelina"), LocalDate.parse("2003-05-18")));

        when(moviesInfoServiceBean.getAllMovieInfos()).thenReturn(Flux.fromIterable(data));

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
        var movieInfo = new MovieInfo("123", "Tails of Dogesh", 2003, List.of("Dogesh", "Dogelina"), LocalDate.parse("2003-05-18"));

        when(moviesInfoServiceBean.getMovieInfoById(MOVIE_INFOS_ID)).thenReturn(Mono.just(movieInfo));

        webTestClient.get()
                .uri(MOVIE_INFOS_URL + "/{id}", MOVIE_INFOS_ID)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody() //another way
                .jsonPath("$.name", "Tails of Dogesh");
    }

    @Test
    void addMovieInfos() {

        var movieInfo = new MovieInfo(null, "Devdas1", 2003, List.of("Dev", "Paaro"), LocalDate.parse("2003-03-18"));

        when(moviesInfoServiceBean.addMovieInfo(movieInfo)).thenReturn(Mono.just(movieInfo));

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
                    assertEquals("Devdas1", result.getName());
                });
    }

    @Test
    void updateMovieInfoById() {

        var movieInfo = new MovieInfo("123", "Devdas Again", 2015, List.of("Christian Balle Balle", "Michael Sugarcane"), LocalDate.parse("2003-03-18"));

        when(moviesInfoServiceBean.updateMovieInfoById(anyString(), any(MovieInfo.class))).thenReturn(Mono.just(movieInfo));

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
    void deleteMovieInfoById() {
        when(moviesInfoServiceBean.deleteMovieInfoById(anyString())).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(MOVIE_INFOS_URL + "/{id}", MOVIE_INFOS_ID)
                .exchange()
                .expectStatus()
                .isNoContent()
                .expectBody(Void.class);
    }

    @Test
    void addMovieInfos_validation() {

        var movieInfo = new MovieInfo(null, "", -2003, List.of(""), LocalDate.parse("2003-03-18"));

        webTestClient.post()
                .uri(MOVIE_INFOS_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var responseBody = movieInfoEntityExchangeResult.getResponseBody();
                    var expectedErrorMsg = "MovieInfo.cast must not be blank,MovieInfo.name must not be blank,MovieInfo.year must be a positive integer";
                    assert responseBody != null;
                    assertEquals(expectedErrorMsg, responseBody);
                });
    }

}