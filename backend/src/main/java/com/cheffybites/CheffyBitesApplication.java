package com.cheffybites;

import com.cheffybites.common.infrastructure.config.FoundationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(FoundationProperties.class)
public class CheffyBitesApplication {

    public static void main(String[] args) {
        SpringApplication.run(CheffyBitesApplication.class, args);
    }
}
