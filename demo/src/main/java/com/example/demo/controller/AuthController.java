package com.example.demo.controller;

import com.example.demo.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*") // Using * temporarily to make testing easier
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // --- Data Transfer Objects (DTOs) ---
    // These must match your JSON keys in Thunder Client exactly
    public record RegisterRequest(String username, String email, String password, String name, String address, String phone) {}
    public record LoginRequest(String email, String password) {}
    public record Verify2FARequest(String email, String code) {}

    // 1. Endpoint: /auth/register
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        try {
            authService.registerUser(
                    request.username(),
                    request.email(),
                    request.password(),
                    request.name(),
                    request.address(),
                    request.phone()
            );
            return ResponseEntity.ok("User registered successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }

    // 2. Endpoint: /auth/login (Triggers 2FA)
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        if (authService.attemptLoginAndSend2FA(request.email(), request.password())) {
            return ResponseEntity.ok("Login successful! 2FA code sent to your email.");
        }
        return ResponseEntity.status(401).body("Invalid email or password.");
    }

    // 3. Endpoint: /auth/2fa/verify
    @PostMapping("/2fa/verify")
    public ResponseEntity<String> verify2FA(@RequestBody Verify2FARequest request) {
        if (authService.verify2FACode(request.email(), request.code())) {
            return ResponseEntity.ok("Authentication successful! Welcome to Bankout.");
        }
        return ResponseEntity.badRequest().body("Invalid or expired 2FA code.");
    }

    // 4. Utility Test Endpoint
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("The Bankout server is up and running!");
    }
}