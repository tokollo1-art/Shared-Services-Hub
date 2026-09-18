package com.ssh.security;

import com.ssh.entity.User;
import com.ssh.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Custom User Details Service
 *
 * Purpose: Load a user from the database for Spring Security authentication.
 *
 * WHY SSH needs this class:
 *   - Bridges Spring Security with the UserRepository
 *   - Called during login to fetch the user by email
 *   - Called on every request by the JWT filter to reload the user
 *   - Maps the User entity's role into a Spring Security authority
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Load a user by their email (used as the username in SSH).
     *
     * @param email the user's email
     * @return a UserDetails object for Spring Security
     * @throws UsernameNotFoundException if the user doesn't exist
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(
                "User not found with email: " + email));

        return org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail())
            .password(user.getPassword())
            .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())))
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(!user.isActive())
            .build();
    }
}
