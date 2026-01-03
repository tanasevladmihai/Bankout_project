package com.blue_eagles.bankout.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many-to-One: Many transactions belong to one Account
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    // Many-to-One: Many transactions belong to one Category
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "transaction_type")
    private String transactionType; // E.g., 'DEPOSIT', 'WITHDRAWAL', 'TRANSFER'

    @Column(name = "transaction_date")
    private Date transactionDate = new Date();

    private String description;

    @Column(name = "store_name")
    private String storeName;
}