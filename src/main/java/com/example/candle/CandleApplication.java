package com.example.candle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CandleApplication {
    public static void main(String[] args) {
        SpringApplication.run(CandleApplication.class, args);
    }
}