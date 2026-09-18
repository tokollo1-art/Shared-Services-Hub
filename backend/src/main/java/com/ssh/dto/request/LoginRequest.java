package com.ssh.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Login Request DTO
 * Purpose: Carries login credentials from the frontend to the backend.
 * WHY SSH needs this DTO:
 *   - Only exposes email and password (not the full User entity)
 *   - Validates input before authentication
 *   - Keeps the API contract clean and explicit
 * TODO: Add "rememberMe" flag if login session persistence is added
 * TODO: Add device metadata for audit logging (optional)
 * TDD: Write LoginRequestTest first
 *       - shouldHaveNoViolations_ForValidRequest()
 *       - shouldFailValidation_WhenEmailIsBlank()
 *       - shouldFailValidation_WhenPasswordIsBlank()
 *       - shouldFailValidation_WhenEmailIsInvalid()
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100)
    private String password;

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public LoginRequest() {}

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // ============================================
    // TO STRING (EXCLUDES PASSWORD)
    // ============================================

    @Override
    public String toString() {
        return "LoginRequest{" +
            "email='" + email + '\'' +
            '}';
    }
}
