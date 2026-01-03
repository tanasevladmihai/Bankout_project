package com.blue_eagles.bankout.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Data
@Table(name = "user_discount_obtained")
public class UserDiscountObtained {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId; // Keeping it simple with ID, or use @ManyToOne User user

    @ManyToOne
    @JoinColumn(name = "offer_id")
    private DiscountOffer offer;

    @Column(name = "usage_code")
    private String usageCode;

    @Column(name = "transaction_id")
    private Long transactionId; // Nullable, filled when used

    private Date obtainedDate = new Date();
}