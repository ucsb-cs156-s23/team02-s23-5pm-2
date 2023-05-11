package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Movie;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.MovieRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

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
public class MoviesController extends ApiController {

    @Autowired
    MovieRepository movieRepository;

    @ApiOperation(value = "List all movies")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Movie> allMovie() {
        Iterable<Movie> movies = movieRepository.findAll();
        return movies;
    }

    @ApiOperation(value = "Get a single movie")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Movie getById(
            @ApiParam("code") @RequestParam Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Movie.class, id));

        return movie;
    }

    @ApiOperation(value = "Create a new movie")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Movie postMovie(
        @ApiParam("movieName") @RequestParam String movieName,
        @ApiParam("directorName") @RequestParam String directorName,
        @ApiParam("releaseDate. e.g. YYYY-mm-dd") @RequestParam String releaseDate
    )
        {

        Movie movie = new Movie();
        movie.setMovieName(movieName);
        movie.setDirectorName(directorName);
        movie.setReleaseDate(null);

        Movie savedMovie = movieRepository.save(movie);
        return savedMovie;
    }

    @ApiOperation(value = "Delete a Movie")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteMovie(
            @ApiParam("id") @RequestParam Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Movie.class, id));

        movieRepository.delete(movie);
        return genericMessage("Movie with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single movie")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Movie updateMovie(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Movie incoming) {

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Movie.class, id));
        
        movie.setDirectorName(incoming.getDirectorName());
        movie.setMovieName(incoming.getMovieName());
        movie.setReleaseDate(incoming.getReleaseDate());

        movieRepository.save(movie);
        return movie;
    }
}
