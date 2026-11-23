package com.example.pizzabolchat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PizzabolchatApplication {

    public static void main(String[] args) {
        SpringApplication.run(PizzabolchatApplication.class, args);
    }

}
