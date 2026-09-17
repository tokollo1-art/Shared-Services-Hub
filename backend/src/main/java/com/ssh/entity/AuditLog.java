package com.ssh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * AuditLog Entity
 * Purpose: Immutable record of every important action in SSH.
 *          Used for compliance, debugging, and impact reporting.
 * WHY SSH needs this entity:
 *   - Corporates and government require verifiable audit trails
 *   - Every state change should be traceable
 *   - Enables "who did what, when" reporting
 *   - Supports fraud detection and dispute resolution
 * TODO: Add action enum (CREATE, UPDATE, DELETE, VERIFY, PAY, LOGIN, LOGOUT)
 * TODO: Add entityType enum (USER, SME, YOUTH, TASK, EXPERIENCE, PAYMENT)
 * TODO: Add method to log from a service layer
 * TODO: Add retention policy (how long to keep logs)
 * TODO: Add @JsonIgnore on details if it contains sensitive data
 * TDD: Write AuditLogEntityTest first
 *       - shouldHaveNoViolations_ForValidLog()
 *       - shouldFailValidation_WhenActionIsBlank()
 *       - shouldSetTimestamp_OnPersist()
 *       - shouldNotAllowUpdate_AfterCreation()
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Many-to-One relationship with User.
     * The user who performed the action. Nullable for system actions.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @NotBlank(message = "Action is required")
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String action; // CREATE, UPDATE, DELETE, VERIFY, PAY, LOGIN, LOGOUT

    @NotBlank(message = "Entity type is required")
    @Size(max = 50)
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType; // USER, SME, YOUTH, TASK, EXPERIENCE, PAYMENT

    @Column(name = "entity_id")
    private Long entityId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Size(max = 45)
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamps are set only once — audit logs are immutable.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public AuditLog() {}

    public AuditLog(User user, String action, String entityType, Long entityId, String details) {
        this.user = user;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // ============================================
    // TO STRING
    // ============================================

    @Override
    public String toString() {
        return "AuditLog{" +
            "id=" + id +
            ", action='" + action + '\'' +
            ", entityType='" + entityType + '\'' +
            ", entityId=" + entityId +
            ", createdAt=" + createdAt +
            '}';
    }
}
