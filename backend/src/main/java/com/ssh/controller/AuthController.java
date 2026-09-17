package com.ssh.controller;

import com.ssh.dto.request.LoginRequest;
import com.ssh.dto.request.RegisterRequest;
import com.ssh.dto.response.ApiResponse;
import com.ssh.dto.response.AuthResponse;
import com.ssh.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Auth Controller
 *
 * Purpose: REST endpoints for register and login.
 *
 * WHY SSH needs this controller:
 *   - Exposes the two public endpoints the frontend hits first
 *   - Delegates all logic to AuthService
 *   - Wraps responses in ApiResponse for consistency
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Register and login endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Register a new user (Youth, SME, Corporate, or Admin).
     * Returns a JWT and the user's profile.
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
        @Valid @RequestBody RegisterRequest request) {

        AuthResponse authResponse = authService.register(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Registration successful", authResponse));
    }

    /**
     * Login with email and password.
     * Returns a JWT and the user's profile.
     */
    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive a JWT")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
        @Valid @RequestBody LoginRequest request) {

        AuthResponse authResponse = authService.login(request);

        return ResponseEntity.ok(
            ApiResponse.success("Login successful", authResponse));
    }
}
