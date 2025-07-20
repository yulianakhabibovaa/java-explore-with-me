package ru.practicum.ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
@ComponentScan(basePackages = {"ru.practicum.stats", "ru.practicum.ewm"})
public class EwmApplication {
    public static void main(String[] args) {
        SpringApplication.run(EwmApplication.class, args);
    }
}