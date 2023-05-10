package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Student;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends CrudRepository<Student, Long> {
}
