package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.EnergyDrink;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.EnergyDrinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import javax.validation.Valid;



@Api(description="Energy Drink")
@RequestMapping("/api/energydrinks")
@RestController
public class EnergyDrinkController extends ApiController{
    @Autowired
    EnergyDrinkRepository energydrinkRepository;

    @ApiOperation(value="List all energy drinks")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<EnergyDrink> allEnergyDrinks(){
        Iterable<EnergyDrink> energydrinks = energydrinkRepository.findAll();
        return energydrinks;
    }

    @ApiOperation(value="Create a new energy drink")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public EnergyDrink postEnergyDrink(
        @ApiParam("name") @RequestParam String name,
        @ApiParam("flavor") @RequestParam String flavor,
        @ApiParam("calories") @RequestParam int calories
    ) throws JsonProcessingException {
        EnergyDrink energydrink = new EnergyDrink();
        energydrink.setName(name);
        energydrink.setFlavor(flavor);
        energydrink.setCalories(calories);
 
        EnergyDrink savedEnergyDrink = energydrinkRepository.save(energydrink);
        return savedEnergyDrink;
    }

    @ApiOperation(value="Get a single energy drink")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public EnergyDrink getById(
        @ApiParam("id") @RequestParam Long id
    ) {
        EnergyDrink energydrink = energydrinkRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(EnergyDrink.class, id));

        return energydrink;
    }

    @ApiOperation(value="Update a single energy drink")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public EnergyDrink updateEnergyDrink(
        @ApiParam("id") @RequestParam Long id,
        @RequestBody @Valid EnergyDrink incoming) {

        EnergyDrink energydrink = energydrinkRepository.findById(id).orElseThrow(()-> new EntityNotFoundException(EnergyDrink.class, id));
        energydrink.setName(incoming.getName());
        energydrink.setFlavor(incoming.getFlavor());
        energydrink.setCalories(incoming.getCalories());

        energydrinkRepository.save(energydrink);
        return energydrink;
    }

    @ApiOperation(value="Delete an energy drink")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteEnergyDrink(
        @ApiParam("id") @RequestParam Long id
    ) {
        EnergyDrink energydrink = energydrinkRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException(EnergyDrink.class, id));
        
        energydrinkRepository.delete(energydrink);
        return genericMessage("Energy drink with id %s deleted".formatted(id));
    }
}
