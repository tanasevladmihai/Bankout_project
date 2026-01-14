package com.blue_eagles.bankout.repository;

import com.blue_eagles.bankout.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    //Optional<User> findByEmail(String email);
    List<Account> findByUserId(Long userId);
}