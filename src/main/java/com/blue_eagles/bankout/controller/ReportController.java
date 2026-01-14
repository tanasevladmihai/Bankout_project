package com.blue_eagles.bankout.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/reports")
public class ReportController {

    // Returns mock data structure for the graph based on period
    @GetMapping("/spending")
    public List<Map<String, Object>> getSpending(@RequestParam String period) {
        // In a real implementation, you would query TransactionRepository
        // using SUM() and GROUP BY category_id

        List<Map<String, Object>> result = new ArrayList<>();

        // Mocking 4 months of data
        String[] months = {"February", "March", "April", "May"};
        Random rand = new Random();

        for (String month : months) {
            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("label", month);
            dataPoint.put("Rent", 15.0 + rand.nextInt(10)); // Mock values matching your image
            dataPoint.put("Food", 10.0 + rand.nextInt(15));
            dataPoint.put("Clothes", 5.0 + rand.nextInt(20));
            result.add(dataPoint);
        }
        return result;
    }
}
