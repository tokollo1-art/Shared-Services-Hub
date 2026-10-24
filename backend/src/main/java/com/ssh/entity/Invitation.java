package com.ssh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Invitation Entity
 *
 * Purpose: Records that an agent invited a youth to a task.
 *
 * One row per (task, youth) pair. A youth may have several pending
 * invitations at once, but they can only be assigned to one task.
 * When the youth is assigned to a task, the other pending invitations
 * are withdrawn — their status becomes WITHDRAWN.
 *
 * The unique constraint on (task_id, youth_id) prevents the same
 * invitation being created twice, even if the matchmaker retries.
 *
 * Statuses:
 *   PENDING   — the youth has been invited, no decision yet
 *   ACCEPTED  — the youth accepted and was assigned (the assignment
 *               itself is on Task.assignedTo; this row records the origin)
 *   WITHDRAWN — another task was assigned first, or the matchmaker
 *               withdrew this invitation
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Entity
@Table(
    name = "invitations",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_invitation_task_youth",
        columnNames = {"task_id", "youth_id"}
    ),
    indexes = {
        @Index(name = "idx_invitation_youth_status", columnList = "youth_id, status"),
        @Index(name = "idx_invitation_task_status", columnList = "task_id, status")
    }
)
public class Invitation {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_ACCEPTED = "ACCEPTED";
    public static final String STATUS_WITHDRAWN = "WITHDRAWN";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "youth_id", nullable = false)
    private Youth youth;

    /**
     * The "why this match" note the agent wrote. Shown to the youth
     * when they see their invitations. Capped at 1000 chars.
     */
    @Size(max = 1000)
    @Column(name = "note", length = 1000)
    private String note;

    @Size(max = 20)
    @Column(name = "status", nullable = false, length = 20)
    private String status = STATUS_PENDING;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = STATUS_PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public Invitation() {}

    public Invitation(Task task, Youth youth, String note) {
        this.task = task;
        this.youth = youth;
        this.note = note;
        this.status = STATUS_PENDING;
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

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Youth getYouth() {
        return youth;
    }

    public void setYouth(Youth youth) {
        this.youth = youth;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    @Override
    public String toString() {
        return "Invitation{" +
            "id=" + id +
            ", taskId=" + (task != null ? task.getId() : null) +
            ", youthId=" + (youth != null ? youth.getId() : null) +
            ", status='" + status + '\'' +
            ", createdAt=" + createdAt +
            '}';
    }
}
