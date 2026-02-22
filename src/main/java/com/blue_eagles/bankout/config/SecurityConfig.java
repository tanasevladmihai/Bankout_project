package com.blue_eagles.bankout.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Public pages (login, register, 2FA)
                        .requestMatchers("/", "/index.html", "/login.html", "/register.html",
                                "/verify.html", "/css/**", "/JS/**", "/auth/**").permitAll()
                        // Protected pages and APIs - require authentication
                        .requestMatchers("/dashboard.html", "/admin.html").authenticated()
                        .requestMatchers("/accounts/**", "/reports/**", "/offers/**", "/api/**", "/budgets/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .sessionManagement(session -> session
                        .maximumSessions(1) // Only one session per user
                        .maxSessionsPreventsLogin(false) // New login invalidates old session
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessUrl("/login.html")
                );

        return http.build();
    }
}


