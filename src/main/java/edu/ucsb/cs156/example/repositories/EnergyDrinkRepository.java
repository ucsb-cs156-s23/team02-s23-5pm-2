package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.EnergyDrink;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnergyDrinkRepository extends CrudRepository<EnergyDrink, Long> {
}
