package com.enterprise.platform.sprint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SprintServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SprintServiceApplication.class, args);
    }
}
