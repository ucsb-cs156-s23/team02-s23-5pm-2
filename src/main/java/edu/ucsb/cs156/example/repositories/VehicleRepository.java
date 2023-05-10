package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Vehicle;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepository extends CrudRepository<Vehicle, Long> {
    Iterable<Vehicle> findAllByBrand(String brand);
}
