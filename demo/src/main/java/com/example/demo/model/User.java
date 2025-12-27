package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; // Good practice for JPA

@Data // Generates getters, setters, toString, etc.
@Entity // Marks this as a JPA Entity (a table in the DB)
@Table(name = "users") // Name of the MySQL table
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id // Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String address;
    private String phone;
    private Double balance;



    public User(String username, String email, String password, String name, String address, String phone) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.balance = 0.0;
    }
}