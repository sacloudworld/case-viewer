package com.example.case_viewer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Semaphore;

@Configuration
public class DatabaseConcurrencyConfig {

    @Bean
    public Semaphore databaseSemaphore() {
        return new Semaphore(5);
    }
}