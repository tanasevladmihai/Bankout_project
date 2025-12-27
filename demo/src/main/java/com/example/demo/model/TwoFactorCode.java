package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "two_factor_codes")
@NoArgsConstructor
public class TwoFactorCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String code;
    private Long expiryTime;

    public TwoFactorCode(String email, String code) {
        this.email = email;
        this.code = code;
        this.expiryTime = System.currentTimeMillis() + 300000; // 5 minutes
    }
}