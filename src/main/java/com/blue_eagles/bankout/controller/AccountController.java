package com.blue_eagles.bankout.controller;

import com.blue_eagles.bankout.entity.Account;
import com.blue_eagles.bankout.entity.Transaction;
import com.blue_eagles.bankout.entity.User;
import com.blue_eagles.bankout.repository.AccountRepository;
import com.blue_eagles.bankout.repository.TransactionRepository;
import com.blue_eagles.bankout.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    @Autowired private AccountRepository accountRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private UserRepository userRepository;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @GetMapping("/my-accounts")
    public List<Account> getAccounts() {
        return accountRepository.findByUserId(getCurrentUserId());
    }

    @PostMapping("/create")
    public Account createAccount(@RequestBody Map<String, String> payload) {
        Account acc = new Account();
        acc.setAccountNumber(payload.get("accountNumber"));
        acc.setAccountName(payload.get("accountName"));
        acc.setBalance(BigDecimal.ZERO);
        acc.setUser(userRepository.findById(getCurrentUserId()).orElseThrow());
        return accountRepository.save(acc);
    }

    @DeleteMapping("/{id}")
    public void deleteAccount(@PathVariable Long id) {
        // Verify the account belongs to the current user
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        if (!account.getUser().getId().equals(getCurrentUserId())) {
            throw new RuntimeException("Unauthorized access to account");
        }
        accountRepository.deleteById(id);
    }

    @PostMapping("/wire-in")
    public void wireIn(@RequestBody Map<String, Object> payload) {
        Long accountId = Long.valueOf(payload.get("accountId").toString());
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());

        Account acc = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Verify account belongs to current user
        if (!acc.getUser().getId().equals(getCurrentUserId())) {
            throw new RuntimeException("Unauthorized access to account");
        }

        acc.setBalance(acc.getBalance().add(amount));
        accountRepository.save(acc);

        // Record Transaction
        Transaction t = new Transaction();
        t.setAccount(acc);
        t.setAmount(amount);
        t.setTransactionType("DEPOSIT");
        t.setDescription("Wire In");
        transactionRepository.save(t);
    }

    @PostMapping("/send-funds")
    public void sendFunds(@RequestBody Map<String, Object> payload) {
        Long accountId = Long.valueOf(payload.get("accountId").toString());
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
        String recipientName = (String) payload.get("recipientName");
        String recipientIban = (String) payload.get("recipientIban");

        Account acc = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Verify account belongs to current user
        if (!acc.getUser().getId().equals(getCurrentUserId())) {
            throw new RuntimeException("Unauthorized access to account");
        }

        // Check balance
        if (acc.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        acc.setBalance(acc.getBalance().subtract(amount));
        accountRepository.save(acc);

        // Record Transaction
        Transaction t = new Transaction();
        t.setAccount(acc);
        t.setAmount(amount.negate());
        t.setTransactionType("TRANSFER");
        t.setDescription("Transfer to " + recipientName + " (" + recipientIban + ")");
        transactionRepository.save(t);
    }
}

