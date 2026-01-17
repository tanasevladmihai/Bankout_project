package com.blue_eagles.bankout.controller;

import com.blue_eagles.bankout.entity.Transaction;
import com.blue_eagles.bankout.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private TransactionRepository transactionRepository;

    @GetMapping("/spending")
    public List<Map<String, Object>> getSpending(@RequestParam String period) {
        // Get all transactions from the database
        List<Transaction> allTransactions = transactionRepository.findAll();

        LocalDate now = LocalDate.now();
        List<Map<String, Object>> result = new ArrayList<>();

        if ("DAILY".equalsIgnoreCase(period)) {
            // Show last 7 days
            for (int i = 6; i >= 0; i--) {
                LocalDate day = now.minusDays(i);
                result.add(buildDataPoint(
                        day.toString(),
                        filterByDate(allTransactions, day, day)
                ));
            }
        } else if ("WEEKLY".equalsIgnoreCase(period)) {
            // Show last 4 weeks
            for (int i = 3; i >= 0; i--) {
                LocalDate weekStart = now.minusWeeks(i).with(java.time.DayOfWeek.MONDAY);
                LocalDate weekEnd = weekStart.plusDays(6);
                result.add(buildDataPoint(
                        "Week " + (4 - i),
                        filterByDate(allTransactions, weekStart, weekEnd)
                ));
            }
        } else { // MONTHLY
            // Show last 4 months
            for (int i = 3; i >= 0; i--) {
                LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
                LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
                result.add(buildDataPoint(
                        monthStart.getMonth().toString(),
                        filterByDate(allTransactions, monthStart, monthEnd)
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
}
