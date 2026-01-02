package com.blue_eagles.bankout.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Data
@Table(name = "users") // "user" is a reserved keyword in Postgres
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String email;
    private String phoneNumber;
    private String passwordHash;
    private Boolean isVerified = false;
    private Date registrationDate = new Date();
}