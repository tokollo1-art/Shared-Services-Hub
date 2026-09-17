package com.ssh.service;

import com.ssh.dto.request.LoginRequest;
import com.ssh.dto.request.RegisterRequest;
import com.ssh.dto.response.AuthResponse;
import com.ssh.dto.response.UserResponse;
import com.ssh.entity.SME;
import com.ssh.entity.User;
import com.ssh.entity.Youth;
import com.ssh.exception.DuplicateResourceException;
import com.ssh.repository.SMERepository;
import com.ssh.repository.UserRepository;
import com.ssh.repository.YouthRepository;
import com.ssh.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Auth Service
 *
 * Purpose: Handles user registration and login for SSH.
 *
 * WHY SSH needs this service:
 *   - Registration: creates User + role-specific profile (Youth or SME)
 *   - Login: authenticates credentials and issues a JWT
 *   - Password hashing is centralised here (via PasswordEncoder)
 *   - Role-specific entities are created at the same time as the User
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final YouthRepository youthRepository;
    private final SMERepository smeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public AuthService(UserRepository userRepository,
                       YouthRepository youthRepository,
                       SMERepository smeRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.youthRepository = youthRepository;
        this.smeRepository = smeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Register a new user and (if applicable) create the role-specific profile.
     * Returns a JWT so the user is immediately logged in after registering.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 1. Reject duplicate emails
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // 2. Create the User with hashed password
        User user = new User(
            request.getName(),
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),
            request.getRole()
        );
        user = userRepository.save(user);

        // 3. Create the role-specific profile entity
        switch (request.getRole().toUpperCase()) {
            case "YOUTH":
                youthRepository.save(new Youth(user));
                break;
            case "SME":
                SME sme = new SME(user, request.getBusinessName());
                sme.setRegistrationNumber(request.getRegistrationNumber());
                sme.setIndustry(request.getIndustry());
                smeRepository.save(sme);
                break;
            default:
                // CORPORATE, ADMIN — no extra profile required yet
                break;
        }

        // 4. Build UserDetails for token generation
        UserDetails userDetails = org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail())
            .password(user.getPassword())
            .authorities("ROLE_" + user.getRole())
            .build();

        // 5. Generate JWT and return
        String token = jwtTokenProvider.generateToken(userDetails);
        UserResponse userResponse = toUserResponse(user);

        return new AuthResponse(token, jwtExpiration, userResponse);
    }

    /**
     * Authenticate an existing user and issue a JWT.
     */
    public AuthResponse login(LoginRequest request) {
        // 1. Let Spring Security verify credentials
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );

        // 2. Load the user record
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new DuplicateResourceException(
                "User not found after successful authentication"));

        // 3. Generate JWT from the authenticated principal
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(userDetails);

        return new AuthResponse(token, jwtExpiration, toUserResponse(user));
    }

    /**
     * Map a User entity to a safe UserResponse DTO (no password).
     */
    private UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole(),
            user.isActive()
        );
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}
