package com.blue_eagles.bankout.repository;

import com.blue_eagles.bankout.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByUserIdAndPeriod(Long userId, String period);
}
