package com.blue_eagles.bankout.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; // IMPORT THIS
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // ADD THIS ANNOTATION
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Allow access to static files (css/js) and public HTML pages
                        .requestMatchers("/", "/index.html", "/login.html", "/register.html",
                                "/verify.html", "/dashboard.html", "/css/**", "/JS/**", "/auth/**",
                                "/accounts/**", "/reports/**", "/api/**", "/offers/**", "/admin.html", "/api/stores/create", "/offers/create").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable()); // Disable default login form
        // .httpBasic(basic -> {}); // Ensure this is commented out or removed

        return http.build();
    }
}

