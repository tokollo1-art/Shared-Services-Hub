package com.ssh.config;

import com.ssh.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration
 *
 * Purpose: Configure Spring Security for SSH.
 *
 * WHY SSH needs this:
 *   - JWT-based stateless authentication (no sessions)
 *   - Role-based access control (Youth, SME, Corporate, Admin)
 *   - BCrypt password hashing
 *   - Public access to auth endpoints and Swagger
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Main security filter chain.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Enable CORS
            .cors(Customizer.withDefaults())

            // Disable CSRF — JWT is stateless, no cookies involved
            .csrf(csrf -> csrf.disable())

            // Stateless session — every request must carry its token
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Return 401 Unauthorized for unauthenticated requests on protected endpoints
            .exceptionHandling(ex ->
                ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))

            // Configure endpoint access
            .authorizeHttpRequests(auth -> auth
                // Let container error dispatches reach Boot's error response instead of
                // being turned into an empty 401 by the security filter chain.
                .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll()
                // Public endpoints
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                // The public verification ledger is readable without a token
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/ledger/**").permitAll()

                // Role-restricted endpoints
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/sme/**").hasRole("SME")
                .requestMatchers("/api/v1/youth/**").hasRole("YOUTH")
                .requestMatchers("/api/v1/corporate/**").hasRole("CORPORATE")

                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            // Register the JWT filter before the standard auth filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCrypt password encoder — used everywhere a password is hashed or verified.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Expose the AuthenticationManager so AuthService can authenticate login attempts.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
