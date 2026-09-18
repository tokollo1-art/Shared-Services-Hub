package com.ssh.dto.response;

import java.time.LocalDateTime;

/**
 * User Response DTO
 *
 * Purpose: Carries user data back to the frontend safely (no password).
 *
 * WHY SSH needs this DTO:
 *   - Never exposes the password field to the client
 *   - Controls exactly which User fields the API returns
 *   - Used by AuthController, UserController, and all profile endpoints
 *   - Includes role so the frontend can render role-specific dashboards
 *
 * TODO: Add optional SME profile summary if role == SME
 * TODO: Add optional Youth profile summary if role == YOUTH
 * TODO: Add avatar URL field
 *
 * TDD: Write UserResponseTest first
 *       - shouldMapAllFields_FromEntity()
 *       - shouldNeverContainPassword()
 *       - shouldIncludeRole()
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
public class UserResponse {

    private Long id;

    private String name;

    private String email;

    private String role;

    private boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public UserResponse() {}

    public UserResponse(Long id, String name, String email, String role, boolean isActive) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.isActive = isActive;
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ============================================
    // TO STRING (SAFE)
    // ============================================

    @Override
    public String toString() {
        return "UserResponse{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", email='" + email + '\'' +
            ", role='" + role + '\'' +
            ", isActive=" + isActive +
            '}';
    }
}
