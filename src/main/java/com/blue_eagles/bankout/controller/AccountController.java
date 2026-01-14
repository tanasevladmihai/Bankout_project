package com.blue_eagles.bankout.controller;

import com.blue_eagles.bankout.entity.Account;
import com.blue_eagles.bankout.entity.Transaction;
import com.blue_eagles.bankout.repository.AccountRepository;
import com.blue_eagles.bankout.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

import java.util.Map;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    @Autowired private AccountRepository accountRepository;
    @Autowired private TransactionRepository transactionRepository;

    @GetMapping("/my_accounts")
    public List<Account> getAccounts() {
        // In a real app, filter by the currently logged-in user

        return accountRepository.findByUserId(1L);
    }

    @PostMapping("/create")
    public Account createAccount(@RequestBody Map<String, String> payload) {
        //return accountRepository.save(account);
        Account acc = new Account();
        acc.setAccountNumber(payload.get("accountNumber"));
        acc.setBalance(BigDecimal.ZERO);
        return accountRepository.save(acc);
    }

    @DeleteMapping("/{id}")
    public void deleteAccount(@PathVariable Long id) {
        accountRepository.deleteById(id);
    }

    @PostMapping("/wire-in")
    public void wireIn(@RequestBody Map<String, Object> payload) {
        Long accountId = Long.valueOf(payload.get("accountId").toString());
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());

        Account acc = accountRepository.findById(accountId).orElseThrow();
        acc.setBalance(acc.getBalance().add(amount));
        accountRepository.save(acc);

        // Record Transaction
        Transaction t = new Transaction();
        t.setAccount(acc);
        t.setAmount(amount); // Positive for deposit
        t.setTransactionType("DEPOSIT");
        t.setDescription("Wire In");
        transactionRepository.save(t);
    }
    @PostMapping("/send-funds")
    public void sendFunds(@RequestBody Map<String, Object> payload) {
        Long accountId = Long.valueOf(payload.get("accountId").toString());
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
        String recipient = (String) payload.get("recipientName");

        Account acc = accountRepository.findById(accountId).orElseThrow();
        // Check balance
        if (acc.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }
        acc.setBalance(acc.getBalance().subtract(amount));
        accountRepository.save(acc);

        // Record Transaction
        Transaction t = new Transaction();
        t.setAccount(acc);
        t.setAmount(amount.negate()); // Negative for expense
        t.setTransactionType("TRANSFER");
        t.setDescription("Transfer to " + recipient);
        transactionRepository.save(t);
    }
}
