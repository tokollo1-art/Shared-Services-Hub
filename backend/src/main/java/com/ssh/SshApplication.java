package com.ssh;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SSH Application - Main Entry Point
 *
 * Purpose: Bootstraps the SSH (Shared Services Hub) platform.
 *
 * TODO: Add @EnableJpaAuditing for automatic timestamp management
 * TODO: Add @EnableScheduling if background jobs are needed
 * TODO: Add @EnableAsync for asynchronous processing (notifications)
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@SpringBootApplication
public class SshApplication {

    public static void main(String[] args) {

        SpringApplication.run(SshApplication.class, args);
    }
}
