package com.blue_eagles.bankout.controller;

import com.blue_eagles.bankout.entity.Account;
import com.blue_eagles.bankout.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    @GetMapping
    public List<Account> getAccounts() {
        // In a real app, filter by the currently logged-in user
        return accountRepository.findAll();
    }

    @PostMapping
    public Account createAccount(@RequestBody Account account) {
        return accountRepository.save(account);
    }

    @GetMapping("/{id}/balance")
    public Double getBalance(@PathVariable Long id) {
        return accountRepository.findById(id)
                .map(account -> account.getBalance().doubleValue())
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }
}