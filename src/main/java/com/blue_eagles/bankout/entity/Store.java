package com.blue_eagles.bankout.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
@Table(name = "store")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String industry;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Relationship: One Store has many Discount Offers
    // "mappedBy" tells Hibernate to look at the 'store' field in the DiscountOffer class
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    private List<DiscountOffer> offers;
}