package com.ssh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SSH Application - Main Entry Point
 *
 * Purpose: Bootstraps the SSH (Shared Services Hub) platform.
 *
 * Annotations:
 *   @EnableAsync      — required by EventPublisher's @Async webhook POST
 *   @EnableScheduling — required by the Tier-widening job and YOUTH_IDLE cooldown
 *
 * TODO: Add @EnableJpaAuditing for automatic timestamp management
 *
 * @author SSH Team
 * @version 1.1
 * @since 2026
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class SshApplication {

    public static void main(String[] args) {
        SpringApplication.run(SshApplication.class, args);
    }
}
