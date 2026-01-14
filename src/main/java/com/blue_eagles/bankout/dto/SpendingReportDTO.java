package com.blue_eagles.bankout.dto;
import lombok.Data;
import java.util.Map;

@Data
public class SpendingReportDTO {
    private String label; // e.g., "January", "Week 1"
    // Map<CategoryName, Amount>
    private Map<String, Double> categoryBreakdown;
}
