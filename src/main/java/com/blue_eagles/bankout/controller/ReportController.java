package com.blue_eagles.bankout.controller;

import com.blue_eagles.bankout.entity.Account;
import com.blue_eagles.bankout.entity.Budget;
import com.blue_eagles.bankout.entity.Transaction;
import com.blue_eagles.bankout.entity.User;
import com.blue_eagles.bankout.repository.AccountRepository;
import com.blue_eagles.bankout.repository.BudgetRepository;
import com.blue_eagles.bankout.repository.TransactionRepository;
import com.blue_eagles.bankout.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @GetMapping("/spending")
    public List<Map<String, Object>> getSpending(@RequestParam String period) {
        // Get current user's accounts
        Long userId = getCurrentUserId();
        List<Account> userAccounts = accountRepository.findByUserId(userId);

        // Get account IDs
        List<Long> accountIds = userAccounts.stream()
                .map(Account::getId)
                .collect(Collectors.toList());

        // Get only transactions belonging to this user's accounts
        List<Transaction> userTransactions = transactionRepository.findAll().stream()
                .filter(t -> accountIds.contains(t.getAccount().getId()))
                .collect(Collectors.toList());

        LocalDate now = LocalDate.now();
        List<Map<String, Object>> result = new ArrayList<>();

        if ("DAILY".equalsIgnoreCase(period)) {
            // Show last 7 days
            for (int i = 6; i >= 0; i--) {
                LocalDate day = now.minusDays(i);
                result.add(buildDataPoint(
                        day.toString(),
                        filterByDate(userTransactions, day, day)
                ));
            }
        } else if ("WEEKLY".equalsIgnoreCase(period)) {
            // Show last 4 weeks
            for (int i = 3; i >= 0; i--) {
                LocalDate weekStart = now.minusWeeks(i).with(java.time.DayOfWeek.MONDAY);
                LocalDate weekEnd = weekStart.plusDays(6);
                result.add(buildDataPoint(
                        "Week " + (4 - i),
                        filterByDate(userTransactions, weekStart, weekEnd)
                ));
            }
        } else { // MONTHLY
            // Show last 4 months
            for (int i = 3; i >= 0; i--) {
                LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
                LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
                result.add(buildDataPoint(
                        monthStart.getMonth().toString(),
                        filterByDate(userTransactions, monthStart, monthEnd)
                ));
            }
        }

        return result;
    }

    private List<Transaction> filterByDate(List<Transaction> transactions, LocalDate start, LocalDate end) {
        return transactions.stream()
                .filter(t -> {
                    if (t.getTransactionDate() == null) return false;
                    LocalDate txDate = t.getTransactionDate().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate();
                    return !txDate.isBefore(start) && !txDate.isAfter(end);
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildDataPoint(String label, List<Transaction> transactions) {
        Map<String, Object> dataPoint = new HashMap<>();
        dataPoint.put("label", label);

        // Group transactions by category name and sum amounts
        Map<String, Double> categoryTotals = transactions.stream()
                .filter(t -> t.getCategory() != null && t.getAmount() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getName(),
                        Collectors.summingDouble(t -> Math.abs(t.getAmount().doubleValue()))
                ));

        // Add each category as a separate key in the map
        categoryTotals.forEach(dataPoint::put);

        return dataPoint;
    }

    @GetMapping("/current-spending")
    public Map<String, Object> getCurrentSpending(@RequestParam String period) {
        Long userId = getCurrentUserId();
        List<Account> userAccounts = accountRepository.findByUserId(userId);

        List<Long> accountIds = userAccounts.stream()
                .map(Account::getId)
                .collect(Collectors.toList());

        List<Transaction> userTransactions = transactionRepository.findAll().stream()
                .filter(t -> accountIds.contains(t.getAccount().getId()))
                .collect(Collectors.toList());

        LocalDate now = LocalDate.now();
        LocalDate start;
        LocalDate end = now;

        if ("DAILY".equalsIgnoreCase(period)) {
            start = now;
        } else if ("WEEKLY".equalsIgnoreCase(period)) {
            start = now.with(java.time.DayOfWeek.MONDAY);
        } else { // MONTHLY
            start = now.withDayOfMonth(1);
        }

        List<Transaction> periodTransactions = filterByDate(userTransactions, start, end);

        // Calculate total spending (sum of negative transactions)
        double totalSpending = periodTransactions.stream()
                .filter(t -> t.getAmount() != null && t.getAmount().compareTo(BigDecimal.ZERO) < 0)
                .mapToDouble(t -> Math.abs(t.getAmount().doubleValue()))
                .sum();

        // Get budget
        Optional<Budget> budget = budgetRepository.findByUserIdAndPeriod(userId, period.toUpperCase());
        double budgetAmount = budget.isPresent() ? budget.get().getAmount().doubleValue() : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("spending", totalSpending);
        result.put("budget", budgetAmount);
        result.put("exceeded", totalSpending > budgetAmount && budgetAmount > 0);
        result.put("hasBudget", budgetAmount > 0);

        return result;
    }
}
