package com.blue_eagles.bankout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync // OPTIONAL: Allows you to use @Async for background tasks
public class BankoutApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankoutApplication.class, args);
    }

}

