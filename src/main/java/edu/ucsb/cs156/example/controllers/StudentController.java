package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Student;
import edu.ucsb.cs156.example.repositories.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import com.fasterxml.jackson.core.JsonProcessingException;



@Api(description="Student")
@RequestMapping("/api/students")
@RestController
public class StudentController extends ApiController{
    @Autowired
    StudentRepository studentRepository;

    @ApiOperation(value="List all students")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Student> allStudents(){
        Iterable<Student> students = studentRepository.findAll();
        return students;
    }

    @ApiOperation(value="Create a new student")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Student postStudent(
        @ApiParam("firstName") @RequestParam String firstName,
        @ApiParam("lastName") @RequestParam String lastName,
        @ApiParam("perm") @RequestParam String perm,
        @ApiParam("email") @RequestParam String email,
        @ApiParam("phoneNumber") @RequestParam String phoneNumber,
        @ApiParam("major") @RequestParam String major
    ) throws JsonProcessingException {
        Student student = new Student();
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setPerm(perm);
        student.setEmail(email);
        student.setPhoneNumber(phoneNumber);
        student.setMajor(major);
 
        Student savedStudent = studentRepository.save(student);
        return savedStudent;
    }
}
