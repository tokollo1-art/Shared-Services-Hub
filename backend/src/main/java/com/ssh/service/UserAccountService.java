package com.ssh.service;

import com.ssh.entity.SME;
import com.ssh.entity.User;
import com.ssh.entity.Youth;
import com.ssh.exception.ResourceNotFoundException;
import com.ssh.repository.SMERepository;
import com.ssh.repository.UserRepository;
import com.ssh.repository.YouthRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/**
 * Resolves the authenticated user and the role-specific profile behind them.
 * Centralised so task, application, payment and wallet services all enforce
 * the same role rules.
 */
@Service
public class UserAccountService {

    private final UserRepository userRepository;
    private final SMERepository smeRepository;
    private final YouthRepository youthRepository;

    public UserAccountService(UserRepository userRepository,
                              SMERepository smeRepository,
                              YouthRepository youthRepository) {
        this.userRepository = userRepository;
        this.smeRepository = smeRepository;
        this.youthRepository = youthRepository;
    }

    public User requireUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    public SME requireSme(String email) {
        return requireSme(requireUser(email));
    }

    public SME requireSme(User user) {
        if (!"SME".equalsIgnoreCase(user.getRole())) {
            throw new AccessDeniedException("Only an SME can perform this action");
        }
        return smeRepository.findByUser(user)
            .orElseThrow(() -> new AccessDeniedException(
                "No SME profile found for user: " + user.getEmail()));
    }

    public Youth requireYouth(String email) {
        return requireYouth(requireUser(email));
    }

    public Youth requireYouth(User user) {
        if (!"YOUTH".equalsIgnoreCase(user.getRole())) {
            throw new AccessDeniedException("Only a youth can perform this action");
        }
        return youthRepository.findByUser(user)
            .orElseThrow(() -> new AccessDeniedException(
                "No youth profile found for user: " + user.getEmail()));
    }

    public Youth requireYouth(Long youthId) {
        return youthRepository.findById(youthId)
            .orElseThrow(() -> new ResourceNotFoundException("Youth", youthId));
    }

    public void requireAnyRole(User user, String... roles) {
        for (String role : roles) {
            if (role.equalsIgnoreCase(user.getRole())) {
                return;
            }
        }
        throw new AccessDeniedException(
            "This action requires one of the following roles: " + String.join(", ", roles));
    }
}
