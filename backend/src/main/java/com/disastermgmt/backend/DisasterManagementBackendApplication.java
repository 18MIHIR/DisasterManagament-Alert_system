package com.disastermgmt.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class DisasterManagementBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DisasterManagementBackendApplication.class, args);
    }

    @Configuration
    @EnableScheduling
    @ConditionalOnProperty(value = "spring.task.scheduling.enabled", matchIfMissing = true)
    public static class SchedulingConfiguration {
    }
}

