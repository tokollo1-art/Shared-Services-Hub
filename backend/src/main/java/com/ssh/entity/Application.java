package com.ssh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Application Entity
 *
 * A youth applies for an OPEN task. The application holds a reservation for
 * that youth for RESERVATION_WINDOW_HOURS (48h). If the SME has not assigned
 * the task by then, the reservation expires and no longer counts against the
 * youth's tier ceiling.
 *
 * One youth may have at most one application per task. Re-applying after a
 * REJECTED, WITHDRAWN or EXPIRED application reactivates the existing row.
 */
@Entity
@Table(
    name = "applications",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_application_task_youth",
        columnNames = {"task_id", "youth_id"}
    )
)
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "youth_id", nullable = false)
    private Youth youth;

    @NotBlank(message = "Status is required")
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String status = ApplicationStatus.PENDING;

    @Size(max = 500)
    @Column(name = "cover_note", length = 500)
    private String coverNote;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * End of the reservation window. Only meaningful while PENDING.
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * When the SME accepted or rejected the application.
     */
    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
        if (this.status == null) {
            this.status = ApplicationStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public Application() {
    }

    public Application(Task task, Youth youth, String coverNote, LocalDateTime expiresAt) {
        this.task = task;
        this.youth = youth;
        this.coverNote = coverNote;
        this.status = ApplicationStatus.PENDING;
        this.expiresAt = expiresAt;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCoverNote() {
        return coverNote;
    }

    public void setCoverNote(String coverNote) {
        this.coverNote = coverNote;
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

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(LocalDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }

    // ============================================
    // STATE TRANSITIONS
    // ============================================

    /**
     * True when this is a PENDING application whose window has passed.
     */
    public boolean isExpired(LocalDateTime now) {
        return ApplicationStatus.PENDING.equals(status)
            && expiresAt != null
            && !expiresAt.isAfter(now);
    }

    public boolean isPending() {
        return ApplicationStatus.PENDING.equals(status);
    }

    public boolean isAccepted() {
        return ApplicationStatus.ACCEPTED.equals(status);
    }

    /**
     * The SME assigned the task to this youth.
     */
    public void accept() {
        requirePending("accept");
        this.status = ApplicationStatus.ACCEPTED;
        this.decidedAt = LocalDateTime.now();
    }

    /**
     * The SME chose another applicant, or the task is no longer available.
     */
    public void reject() {
        requirePending("reject");
        this.status = ApplicationStatus.REJECTED;
        this.decidedAt = LocalDateTime.now();
    }

    /**
     * The youth withdrew their application.
     */
    public void withdraw() {
        requirePending("withdraw");
        this.status = ApplicationStatus.WITHDRAWN;
        this.decidedAt = LocalDateTime.now();
    }

    /**
     * The reservation window elapsed without a decision.
     */
    public void expire() {
        requirePending("expire");
        this.status = ApplicationStatus.EXPIRED;
        this.decidedAt = LocalDateTime.now();
    }

    /**
     * Re-open an application that is allowed to be re-submitted.
     */
    public void reactivate(String coverNote, LocalDateTime newExpiresAt) {
        if (!ApplicationStatus.REAPPLICABLE.contains(this.status)) {
            throw new IllegalStateException(
                "Only REJECTED, WITHDRAWN or EXPIRED applications can be re-submitted");
        }
        this.status = ApplicationStatus.PENDING;
        this.coverNote = coverNote;
        this.expiresAt = newExpiresAt;
        this.decidedAt = null;
    }

    private void requirePending(String action) {
        if (!isPending()) {
            throw new IllegalStateException(
                "Cannot " + action + " an application in status " + status);
        }
    }

    @Override
    public String toString() {
        return "Application{" +
            "id=" + id +
            ", taskId=" + (task != null ? task.getId() : null) +
            ", youthId=" + (youth != null ? youth.getId() : null) +
            ", status='" + status + '\'' +
            ", expiresAt=" + expiresAt +
            '}';
    }
}
