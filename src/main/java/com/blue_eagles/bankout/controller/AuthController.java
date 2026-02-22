package com.blue_eagles.bankout.controller;

import com.blue_eagles.bankout.dto.*;
import com.blue_eagles.bankout.entity.User;
import com.blue_eagles.bankout.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Map<String, String> twoFactorStorage = new HashMap<>();

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already registered");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setIsVerified(false);
        userRepository.save(user);

        String code = String.valueOf((int)(Math.random() * 9000) + 1000);
        twoFactorStorage.put(request.getEmail(), code);
        System.out.println("2FA CODE FOR " + request.getEmail() + ": " + code);

        return ResponseEntity.ok("User registered. Please verify 2FA.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isPresent() && passwordEncoder.matches(request.getPassword(), userOpt.get().getPasswordHash())) {
            String code = String.valueOf((int)(Math.random() * 9000) + 1000);
            twoFactorStorage.put(request.getEmail(), code);
            System.out.println("2FA CODE FOR " + request.getEmail() + ": " + code);

            return ResponseEntity.ok("Credentials valid. 2FA sent.");
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verify2FA(@RequestBody TwoFactorRequest request, HttpServletRequest httpRequest) {
        String serverCode = twoFactorStorage.get(request.getEmail());
        if (serverCode != null && serverCode.equals(request.getCode())) {
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setIsVerified(true);
                userRepository.save(user);

                // CREATE SESSION - This authenticates the user
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                user.getEmail(),
                                null,
                                Collections.emptyList()
                        );
                SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                securityContext.setAuthentication(authToken);
                SecurityContextHolder.setContext(securityContext);

                // Store security context in HTTP session
                HttpSession session = httpRequest.getSession(true);
                session.setAttribute(
                        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                        securityContext
                );

                System.out.println("Session created for user: " + user.getEmail() + " (ID: " + session.getId() + ")");
            }
            twoFactorStorage.remove(request.getEmail());
            return ResponseEntity.ok("Login Successful");
        }
        return ResponseEntity.status(400).body("Invalid Code");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.ok(Map.of(
                    "authenticated", true,
                    "email", auth.getName()
            ));
        }
        return ResponseEntity.status(401).body(Map.of("authenticated", false));
    }
}


