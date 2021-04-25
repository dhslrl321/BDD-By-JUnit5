package com.example.tokenauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class TokenAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(TokenAuthApplication.class, args);
    }


}
