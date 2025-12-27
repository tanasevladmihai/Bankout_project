package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.model.TwoFactorCode;
import com.example.demo.repository.TwoFactorRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TwoFactorRepository twoFactorRepository;
    private final org.springframework.mail.javamail.JavaMailSender mailSender;

    @Autowired
    public AuthService(UserRepository userRepository,
                       TwoFactorRepository twoFactorRepository,
                       org.springframework.mail.javamail.JavaMailSender mailSender) { // ADD THIS
        this.userRepository = userRepository;
        this.twoFactorRepository = twoFactorRepository;
        this.mailSender = mailSender; // INITIALIZE IT
    }

    private String generateRandomCode() {
        java.util.Random random = new java.util.Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    // 1. Logic for /auth/register
    public User registerUser(String username, String email, String password, String name, String address, String phone) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Error: Username is already taken!");
        }
        User newUser = new User(username, email, password, name, address, phone);
        return userRepository.save(newUser);
    }

    // 2. Logic for /auth/login, /auth/2fa/send
    public boolean attemptLoginAndSend2FA(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (user.getPassword().equals(password)) {
                String code = generateRandomCode();

                twoFactorRepository.deleteByEmail(email);
                TwoFactorCode tfaCode = new TwoFactorCode(email, code);
                twoFactorRepository.save(tfaCode);

                // --- THE FINAL STEP ---
                sendEmail(user.getEmail(), user.getUsername(), code);

                System.out.println("DEBUG: 2FA Code sent to " + email);
                return true;
            }
        }
        return false;
    }

    // 3. Logic for /auth/2fa/verify
    public boolean verify2FACode(String email, String code) {
        // NEW LOGIC: Look in the TwoFactorRepository, not the User table
        Optional<TwoFactorCode> tfaOptional = twoFactorRepository.findByEmail(email);

        if (tfaOptional.isPresent()) {
            TwoFactorCode tfa = tfaOptional.get();

            if (tfa.getCode().equals(code)) {
                if (System.currentTimeMillis() < tfa.getExpiryTime()) {
                    twoFactorRepository.deleteByEmail(email); // Success, remove code
                    return true;
                }
            }
        }
        return false;
    }

    private void sendEmail(String toEmail, String username, String code) {
        org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your Bankout Verification Code");
        message.setText("Hello " + username + ",\n\n" +
                "Your security code is: " + code + "\n" +
                "This code will expire in 5 minutes. If you did not request this, please ignore this email.\n\n" +
                "Best regards,\n" +
                "The Bankout Team");
        mailSender.send(message);
    }
}