package edu.ucsb.cs156.example.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.GenerationType;
import javax.persistence.Column;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "student")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotNull
    private String firstName;
    
    @NotNull
    private String lastName;

    @NotNull
    @Column(unique=true)
    private long perm;

    @NotNull
    @Column(unique=true)
    private String email;

    @NotNull
    private String phoneNumber;

    @NotNull
    private String major;
}
