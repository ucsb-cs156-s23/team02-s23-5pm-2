package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Restaurant;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RestaurantRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

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

@Api(description = "Restaurant")
@RequestMapping("/api/restaurants")
@RestController
@Slf4j
public class RestaurantController extends ApiController {
    
    @Autowired
    RestaurantRepository restaurantRepository;

    @ApiOperation(value = "List all restaurants")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Restaurant> allRestaurantss() {
        Iterable<Restaurant> restaurants = restaurantRepository.findAll();
        return restaurants;
    }

    @ApiOperation(value = "Get a restaurant")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Restaurant getById(
            @ApiParam("id") @RequestParam Long id) {
            Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Restaurant.class, id));
        return restaurant;
    }

    @ApiOperation(value = "Create a new restaurant")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Restaurant postRestaurants(
        @ApiParam("name") @RequestParam String name,
        @ApiParam("location") @RequestParam String location,
        @ApiParam("hasTakeOut") @RequestParam boolean hasTakeOut
        )
        {

        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setLocation(location);
        restaurant.setHasTakeOut(hasTakeOut);

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        return savedRestaurant;
    }

    @ApiOperation(value = "Delete a restaurant")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteRestaurant(
            @ApiParam("id") @RequestParam Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Restaurant.class, id));

        restaurantRepository.delete(restaurant);
        return genericMessage("Restaurant with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single restaurants")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Restaurant updateRestaurants(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Restaurant incoming) {

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Restaurant.class, id));


        restaurant.setName(incoming.getName());  
        restaurant.setLocation(incoming.getLocation());
        restaurant.setHasTakeOut(incoming.getHasTakeOut());

        restaurantRepository.save(restaurant);

        return restaurant;
    }
}
