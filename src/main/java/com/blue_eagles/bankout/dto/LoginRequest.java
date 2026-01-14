package com.blue_eagles.bankout.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
