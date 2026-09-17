package com.ssh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * User Entity
 * Purpose: Represents any person who can log into SSH.
 *          Role determines what they can do (YOUTH, SME, CORPORATE, ADMIN).
 * WHY SSH needs this entity:
 *   - Single authentication point for all user types
 *   - Role-based access control (RBAC)
 *   - Base for Youth, SME, Corporate profile entities
 * TODO: Consider moving role to an enum (@Enumerated(EnumType.STRING))
 * TODO: Add relationship to Youth, SME, Corporate profiles
 * TODO: Add @JsonIgnore to password field
 * TDD: Write UserEntityTest first
 *       - shouldHaveNoViolations_ForValidUser()
 *       - shouldFailValidation_WhenEmailIsBlank()
 *       - shouldFailValidation_WhenEmailIsInvalid()
 *       - shouldFailValidation_WhenNameTooLong()
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100)
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Role is required")
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String role; // YOUTH, SME, CORPORATE, ADMIN

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Called before the entity is persisted (inserted).
     * Sets createdAt and updatedAt automatically.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Called before the entity is updated.
     * Refreshes updatedAt automatically.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public User() {}

    public User(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.isActive = true;
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
    // TO STRING
    // ============================================

    /**
     * Returns a readable string representation of the user.
     * Excludes password for security.
     */
    @Override
    public String toString() {
        return "User{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", email='" + email + '\'' +
            ", role='" + role + '\'' +
            ", isActive=" + isActive +
            '}';
    }
}
