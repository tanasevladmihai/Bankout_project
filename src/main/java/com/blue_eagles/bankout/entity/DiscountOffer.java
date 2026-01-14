package com.blue_eagles.bankout.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
public class DiscountOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "store_id")
    @JsonIgnore
    private Store store; // You must also create the Store entity similar to above

    private String title;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private Date expiryDate;
}