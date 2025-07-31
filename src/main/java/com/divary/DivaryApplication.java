package com.divary;

import com.divary.global.config.properties.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DivaryApplication {

    public static void main(String[] args) {
        SpringApplication.run(DivaryApplication.class, args);
    }

} 