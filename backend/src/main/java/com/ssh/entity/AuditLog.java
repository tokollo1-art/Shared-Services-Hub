package com.ssh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * AuditLog Entity
 *
 * Purpose: Immutable record of every important action in SSH.
 *          Used for compliance, debugging, and impact reporting.
 *
 * Two kinds of rows live here:
 *   1. User actions (actorType = USER): who changed what
 *   2. Agent actions (actorType = AGENT): what the AI proposed,
 *      with its reasoning, output, and fallback flag
 *
 * Slot 2 changes:
 *   - Added actorType, agentName, inputs, reasoning, output,
 *     usedFallback, modelVersion, idempotencyKey, taskId, youthId
 *   - Added triggerEventId — the webhook event that caused this action
 *   - Added three indexes for fast activity feed queries
 *
 * @author SSH Team
 * @version 1.2
 * @since 2026
 */
@Entity
@Table(
    name = "audit_logs",
    indexes = {
        @Index(name = "idx_audit_task", columnList = "task_id"),
        @Index(name = "idx_audit_youth", columnList = "youth_id"),
        @Index(name = "idx_audit_created", columnList = "created_at")
    }
)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * USER or AGENT. The agent activity feed filters on this field.
     */
    @Size(max = 10)
    @Column(name = "actor_type", length = 10)
    private String actorType = "USER";

    /**
     * When actorType = AGENT, the agent's name:
     * "matchmaker", "evidence", "invitation", "elicitation".
     */
    @Size(max = 64)
    @Column(name = "agent_name", length = 64)
    private String agentName;

    @NotBlank(message = "Action is required")
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String action;

    @NotBlank(message = "Entity type is required")
    @Size(max = 50)
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    /**
     * The webhook event UUID that triggered this action.
     * For elicitation, this is the elicitationId.
     * Null for user actions that are not event-driven.
     */
    @Size(max = 36)
    @Column(name = "trigger_event_id", length = 36)
    private String triggerEventId;

    @Column(name = "inputs", columnDefinition = "TEXT")
    private String inputs;

    @Column(name = "reasoning", columnDefinition = "TEXT")
    private String reasoning;

    @Column(name = "output", columnDefinition = "TEXT")
    private String output;

    @Column(name = "used_fallback")
    private boolean usedFallback = false;

    @Size(max = 100)
    @Column(name = "model_version", length = 100)
    private String modelVersion;

    @Size(max = 200)
    @Column(name = "idempotency_key", unique = true, length = 200)
    private String idempotencyKey;

    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "youth_id")
    private Long youthId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Size(max = 45)
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
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
        this.actorType = "USER";
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

    public String getActorType() {
        return actorType;
    }

    public void setActorType(String actorType) {
        this.actorType = actorType;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
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

    public String getTriggerEventId() {
        return triggerEventId;
    }

    public void setTriggerEventId(String triggerEventId) {
        this.triggerEventId = triggerEventId;
    }

    public String getInputs() {
        return inputs;
    }

    public void setInputs(String inputs) {
        this.inputs = inputs;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public boolean isUsedFallback() {
        return usedFallback;
    }

    public void setUsedFallback(boolean usedFallback) {
        this.usedFallback = usedFallback;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getYouthId() {
        return youthId;
    }

    public void setYouthId(Long youthId) {
        this.youthId = youthId;
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
            ", actorType='" + actorType + '\'' +
            ", agentName='" + agentName + '\'' +
            ", action='" + action + '\'' +
            ", entityType='" + entityType + '\'' +
            ", entityId=" + entityId +
            ", triggerEventId='" + triggerEventId + '\'' +
            ", usedFallback=" + usedFallback +
            ", modelVersion='" + modelVersion + '\'' +
            ", createdAt=" + createdAt +
            '}';
    }
}
