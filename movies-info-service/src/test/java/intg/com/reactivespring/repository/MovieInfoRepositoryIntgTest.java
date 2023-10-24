package com.reactivespring.repository;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
@ActiveProfiles("test")
class MovieInfoRepositoryIntgTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @BeforeEach
    void beforeAll() {

        var data = List.of(new MovieInfo(null, "Devdas", 2003, List.of("Dev", "Paaro"), LocalDate.parse("2003-03-18")),
                new MovieInfo(null, "Transformers", 2003, List.of("Bumblebee", "Optimus Prime"), LocalDate.parse("2003-04-18")),
                new MovieInfo("123", "Tails of Dogesh", 2003, List.of("Dogesh", "Dogelina"), LocalDate.parse("2003-05-18")));

        movieInfoRepository.saveAll(data).blockLast();
    }

    @AfterEach
    void teardown(){

        movieInfoRepository.deleteAll().block();
    }

    @Test
    void findAll() {

        var result = movieInfoRepository.findAll().log();

        StepVerifier.create(result)
                .expectNextCount(3)
                .verifyComplete();

    }

    @Test
    void findById() {

        var result = movieInfoRepository.findById("123").log();

        StepVerifier.create(result)
                .assertNext(movieInfo -> {
                    assertEquals("Tails of Dogesh", movieInfo.getName());
                })
                .verifyComplete();

    }

    @Test
    void save() {

        var krishhMovie = new MovieInfo(null, "Krishh", 2003, List.of("Jaddu", "Aww Aww"), LocalDate.parse("2003-03-18"));

        var result = movieInfoRepository.save(krishhMovie).log();

        StepVerifier.create(result)
                .assertNext(movieInfo -> {
                    assertNotNull(movieInfo.getMovieInfoId());
                    assertEquals("Krishh", movieInfo.getName());
                })
                .verifyComplete();

    }

    @Test
    void update() {

        var dogeshMovie = movieInfoRepository.findById("123").block();
        assert dogeshMovie != null;
        dogeshMovie.setYear(2023);

        var result = movieInfoRepository.save(dogeshMovie).log();

        StepVerifier.create(result)
                .assertNext(movieInfo -> {
                    assertEquals(2023, movieInfo.getYear());
                })
                .verifyComplete();

    }

    @Test
    void delete() {

        movieInfoRepository.deleteById("123").block();
        var result = movieInfoRepository.findAll().log();

        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();

    }

}