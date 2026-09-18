package com.ssh.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Register Request DTO
 *
 * Purpose: Carries user registration data from the frontend to the backend.
 *
 * WHY SSH needs this DTO:
 *   - Only exposes fields the client should submit
 *   - Validates input before creating a User
 *   - Supports all roles: YOUTH, SME, CORPORATE, ADMIN
 *   - Prevents clients from setting restricted fields (like id, isActive)
 *
 * TODO: Add role-specific nested DTOs (SMEProfileRequest, YouthProfileRequest)
 * TODO: Add optional phone number field
 * TODO: Add email verification token flow
 *
 * TDD: Write RegisterRequestTest first
 *       - shouldHaveNoViolations_ForValidRequest()
 *       - shouldFailValidation_WhenNameIsBlank()
 *       - shouldFailValidation_WhenEmailIsInvalid()
 *       - shouldFailValidation_WhenPasswordTooShort()
 *       - shouldFailValidation_WhenRoleIsInvalid()
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    @NotBlank(message = "Role is required")
    @Pattern(
        regexp = "YOUTH|SME|CORPORATE|ADMIN",
        message = "Role must be one of: YOUTH, SME, CORPORATE, ADMIN"
    )
    private String role;

    // ============================================
    // SME-SPECIFIC FIELDS (nullable)
    // ============================================

    @Size(max = 100)
    private String businessName;

    @Size(max = 20)
    private String registrationNumber;

    @Size(max = 100)
    private String industry;

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public RegisterRequest() {}

    public RegisterRequest(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    // ============================================
    // TO STRING (EXCLUDES PASSWORD)
    // ============================================

    @Override
    public String toString() {
        return "RegisterRequest{" +
            "name='" + name + '\'' +
            ", email='" + email + '\'' +
            ", role='" + role + '\'' +
            '}';
    }
}
