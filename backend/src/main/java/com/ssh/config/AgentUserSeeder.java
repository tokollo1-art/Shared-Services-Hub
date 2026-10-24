package com.ssh.config;

import com.ssh.entity.User;
import com.ssh.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * AgentUserSeeder
 *
 * Purpose: Creates the AGENT user on startup if it doesn't exist.
 *          Reads credentials from environment variables.
 *
 * Production behaviour:
 *   - If AGENT_EMAIL or AGENT_PASSWORD is absent, logs a warning and
 *     does not seed. The app still starts; agent endpoints return 403
 *     until credentials are configured.
 *   - Idempotent: findByEmail first, skip if present.
 *   - Password is BCrypt-hashed by the injected PasswordEncoder.
 *
 * The seeded user has role = "AGENT". Spring Security's hasRole("AGENT")
 * matcher expects that exact string.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Component
public class AgentUserSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AgentUserSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String agentEmail;
    private final String agentPassword;
    private final String agentName;

    public AgentUserSeeder(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        @Value("${AGENT_EMAIL:}") String agentEmail,
        @Value("${AGENT_PASSWORD:}") String agentPassword,
        @Value("${AGENT_NAME:SSH Agent}") String agentName
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.agentEmail = agentEmail;
        this.agentPassword = agentPassword;
        this.agentName = agentName;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (agentEmail == null || agentEmail.isBlank()
            || agentPassword == null || agentPassword.isBlank()) {
            log.warn(
                "AGENT_EMAIL or AGENT_PASSWORD not set. Skipping agent user seed. "
                    + "Agent endpoints will return 403 until credentials are configured."
            );
            return;
        }

        if (userRepository.findByEmail(agentEmail).isPresent()) {
            log.info("Agent user already exists: {}", agentEmail);
            return;
        }

        User agent = new User();
        agent.setName(agentName);
        agent.setEmail(agentEmail);
        agent.setPassword(passwordEncoder.encode(agentPassword));
        agent.setRole("AGENT");
        agent.setActive(true);

        userRepository.save(agent);
        log.info("Agent user seeded: {}", agentEmail);
    }
}
