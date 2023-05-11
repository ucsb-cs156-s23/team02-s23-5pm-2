package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Movie;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.MovieRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@Api(description = "Movie")
@RequestMapping("/api/movie")
@RestController
@Slf4j
public class MoviesController extends ApiController {

    @Autowired
    MovieRepository MovieRepository;

    @ApiOperation(value = "List all movies")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Movie> allMovie() {
        Iterable<Movie> movies = MovieRepository.findAll();
        return movies;
    }

    @ApiOperation(value = "Get a single movie")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Movie getById(
            @ApiParam("code") @RequestParam String code) {
        Movie movies = MovieRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(Movie.class, code));

        return movies;
    }

    @ApiOperation(value = "Create a new movie")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Movie postMovie(
        @ApiParam("code") @RequestParam String code,
        @ApiParam("name") @RequestParam String name,
        @ApiParam("isAction") @RequestParam boolean isAction,
        @ApiParam("isHorror") @RequestParam boolean isHorror,
        @ApiParam("isComedy") @RequestParam boolean isComedy,
        @ApiParam("rottenTomatoesScore") @RequestParam double rottenTomatoesScore,
        @ApiParam("criticScore") @RequestParam double criticScore
        )
        {

        Movie movies = new Movie();
        commons.setCode(code);
        commons.setName(name);
        commons.setIsAction(isAction);
        commons.setIsHorror(isHorror);
        commons.setIsComedy(isComedy);
        commons.setRottenTomatoesScore(rottenTomatoesScore);
        commons.setCriticScore(criticScore);

        Movie movies = movieRepository.save(movies);

        return movies;
    }

    @ApiOperation(value = "Delete a Movie")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteMovie(
            @ApiParam("code") @RequestParam String code) {
        Movie movie = movieRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(Movie.class, code));

        movieRepository.delete(movies);
        return genericMessage("<Movie> with id %s deleted".formatted(code));
    }

    @ApiOperation(value = "Update a single movie")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Movie updateMovie(
            @ApiParam("code") @RequestParam String code,
            @RequestBody @Valid Movie incoming) {

        Movie movie = movieRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(Movie.class, code));


        commons.setName(incoming.getName());  
        commons.setIsAction(incoming.getIsAction());
        commons.setIsHorror(incoming.getIsHorror());
        commons.setIsComedy(incoming.getIsComedy());
        commons.setRottenTomatoesScore(incoming.getRottenTomatoesScore());
        commons.setCriticScore(incoming.getCriticScore());

        movieRepository.save(movie);

        return movie;
    }
}
