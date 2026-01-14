package com.blue_eagles.bankout.dto;

import lombok.Data;

@Data
public class TwoFactorRequest {
    private String email;
    private String code;
}