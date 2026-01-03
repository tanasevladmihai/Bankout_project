package com.blue_eagles.bankout.repository;

import com.blue_eagles.bankout.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Find all transactions for a specific account
    List<Transaction> findByAccountId(Long accountId);

    // Find latest 5 transactions for an account (useful for dashboard)
    List<Transaction> findTop5ByAccountIdOrderByTransactionDateDesc(Long accountId);
}