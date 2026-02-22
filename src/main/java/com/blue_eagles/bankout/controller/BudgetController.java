package com.blue_eagles.bankout.controller;

import com.blue_eagles.bankout.entity.Budget;
import com.blue_eagles.bankout.entity.User;
import com.blue_eagles.bankout.repository.BudgetRepository;
import com.blue_eagles.bankout.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/budgets")
public class BudgetController {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @GetMapping("/{period}")
    public ResponseEntity<?> getBudget(@PathVariable String period) {
        Long userId = getCurrentUserId();
        Optional<Budget> budget = budgetRepository.findByUserIdAndPeriod(userId, period.toUpperCase());

        if (budget.isPresent()) {
            return ResponseEntity.ok(budget.get());
        }
        return ResponseEntity.ok(Map.of("amount", 0));
    }

    @PostMapping("/set")
    public ResponseEntity<?> setBudget(@RequestBody Map<String, Object> payload) {
        Long userId = getCurrentUserId();
        String period = ((String) payload.get("period")).toUpperCase();
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());

        Optional<Budget> existingBudget = budgetRepository.findByUserIdAndPeriod(userId, period);

        Budget budget;
        if (existingBudget.isPresent()) {
            budget = existingBudget.get();
            budget.setAmount(amount);
        } else {
            budget = new Budget();
            budget.setUserId(userId);
            budget.setPeriod(period);
            budget.setAmount(amount);
        }

        budgetRepository.save(budget);
        return ResponseEntity.ok(budget);
    }
}
