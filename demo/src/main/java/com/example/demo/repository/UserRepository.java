package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// This interface extends JpaRepository<EntityClass, IdType>
// It provides all CRUD (Create, Read, Update, Delete) methods automatically.
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA automatically implements this method just by its name!
    // We need this to look up a user when they try to log in.
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
}