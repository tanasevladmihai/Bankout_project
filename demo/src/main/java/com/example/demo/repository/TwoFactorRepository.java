package com.example.demo.repository;

import com.example.demo.model.TwoFactorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface TwoFactorRepository extends JpaRepository<TwoFactorCode, Long> {
    Optional<TwoFactorCode> findByEmail(String email);

    @Transactional
    void deleteByEmail(String email);
}