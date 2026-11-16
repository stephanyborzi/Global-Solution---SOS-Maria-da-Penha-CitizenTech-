package com.citizentech.sos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SosApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(SosApiApplication.class, args);
    }
}
