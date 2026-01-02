package com.blue_eagles.bankout.repository;

import com.blue_eagles.bankout.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}