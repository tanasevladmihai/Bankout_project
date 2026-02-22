package com.blue_eagles.bankout.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data
@Table(name = "budgets")
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "period", nullable = false)
    private String period; // 'DAILY', 'WEEKLY', 'MONTHLY'

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "category_id")
    private Long categoryId; // Optional: for future category-specific budgets
}