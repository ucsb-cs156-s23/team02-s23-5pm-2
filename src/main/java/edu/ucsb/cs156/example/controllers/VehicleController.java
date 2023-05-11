package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Vehicle;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.VehicleRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDateTime;

@Api(description = "Vehicle")
@RequestMapping("/api/vehicle")
@RestController
@Slf4j
public class VehicleController extends ApiController {
    
    @Autowired
    VehicleRepository vehicleRepository;

    @ApiOperation(value = "List all vehicles")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Vehicle> allCommonss() {
        Iterable<Vehicle> vehicles = vehicleRepository.findAll();
        return vehicles;
    }

    @ApiOperation(value = "Get a vehicle")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Vehicle getById(
            @ApiParam("id") @RequestParam Long id) {
            Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Vehicle.class, id));
        return vehicle;
    }

    @ApiOperation(value = "Create a new vehicle")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Vehicle postVehicle(
        @ApiParam("brand") @RequestParam String brand,
        @ApiParam("model") @RequestParam String model,
        @ApiParam("licence") @RequestParam String licence,
        @ApiParam("year") @RequestParam String year) throws JsonProcessingException
        {
            log.info("brand={}", brand);
            log.info("model={}", model);
            log.info("licence={}", licence);
            log.info("year={}", year);
            
            Vehicle vehicle = new Vehicle();
            vehicle.setBrand(brand);
            vehicle.setModel(model);
            vehicle.setLicence(licence);
            vehicle.setYear(year);
            Vehicle savedVehicle = vehicleRepository.save(vehicle);
            
            return savedVehicle;
        }

    @ApiOperation(value = "Delete a vehicle")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteVehicle(
            @ApiParam("id") @RequestParam Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Vehicle.class, id));

        vehicleRepository.delete(vehicle);
        return genericMessage("Vehicle with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single commons")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Vehicle updateCommons(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Vehicle incoming) {

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Vehicle.class, id));


        vehicle.setBrand(incoming.getBrand());  
        vehicle.setModel(incoming.getModel());
        vehicle.setLicence(incoming.getLicence());
        vehicle.setYear(incoming.getYear());

        vehicleRepository.save(vehicle);

        return vehicle;
    }
}