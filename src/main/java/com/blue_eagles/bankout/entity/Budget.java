package com.blue_eagles.bankout.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "limit_amount")
    private BigDecimal limitAmount;

    @Column(name = "period_type")
    private String periodType; // 'WEEKLY', 'MONTHLY'
}