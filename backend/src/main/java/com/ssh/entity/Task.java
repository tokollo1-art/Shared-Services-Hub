package com.ssh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.ssh.exception.InvalidTaskTransitionException;
import java.time.LocalDateTime;

/**
 * Task Entity
 * Purpose: Represents a task or opportunity posted by an SME.
 *          Tasks are the core unit of work in the SSH Shared Services Hub.
 * WHY SSH needs this entity:
 *   - SMEs post tasks (free access)
 *   - Youth complete tasks and earn income
 *   - Verified tasks become entries in the Experience Ledger
 *   - Tasks drive payment calculations
 *
 * Slot 2 changes:
 *   - Added tier (int) — Slot 1 tier rules, youth feed, matchmaker
 *   - Added reservedUntil (timestamp) — Tier 0 window, widening job
 *   - Added remote (boolean) — feed filter, eligibility
 *   - Added location (string) — distance bucket for matchmaker
 *   - Added estHours (double) — required by Python's TaskPayload
 *
 * TODO: Add status enum (OPEN, IN_PROGRESS, COMPLETED, VERIFIED, CANCELLED)
 * TODO: Add category enum (ADMIN, TECH, DESIGN, MARKETING, etc.)
 * TODO: Add method to check if task is expired
 * TODO: Add relationship to Payment entity (1:1)
 * TODO: Add relationship to Experience entity (1:1)
 * TDD: Write TaskEntityTest first
 *       - shouldHaveNoViolations_ForValidTask()
 *       - shouldFailValidation_WhenTitleIsBlank()
 *       - shouldFailValidation_WhenTitleTooLong()
 *       - shouldNotBeAssigned_WhenAlreadyAssigned()
 *
 * @author SSH Team
 * @version 1.2
 * @since 2026
 */
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Many-to-One relationship with SME.
     * Many tasks can belong to one SME.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sme_id", nullable = false)
    private SME sme;

    /**
     * Many-to-One relationship with Youth.
     * A task may be assigned to one youth (nullable until assigned).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private Youth assignedTo;

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String title;

    @Size(max = 1000)
    @Column(length = 1000)
    private String description;

    @Size(max = 50)
    @Column(length = 50)
    private String category;

    @NotBlank(message = "Status is required")
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String status = "OPEN";
     // See TaskStatus for the allowed lifecycle

    @Column
    private Double budget;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(name = "is_paid")
    private boolean isPaid = true;

    /**
     * The SME's reason for sending the last submission back for revision.
     * Kept until the task is verified so the youth can read it while revising.
     */
    @Size(max = 1000)
    @Column(name = "revision_reason", length = 1000)
    private String revisionReason;

    /**
     * Number of times the SME has sent this task back for revision.
     */
    @Column(name = "revision_count", nullable = false)
    private int revisionCount = 0;

    /**
     * Tier of the task.
     * 0 = entry level (reserved window for new youth)
     * 1 = intermediate
     * 2 = advanced
     */
    @Column(name = "tier")
    private Integer tier;

    /**
     * When the Tier 0 reservation window ends.
     * After this timestamp, an unclaimed Tier 0 task is widened to Tier 1.
     */
    @Column(name = "reserved_until")
    private LocalDateTime reservedUntil;

    /**
     * Whether the task can be done remotely.
     */
    @Column(name = "remote")
    private boolean remote = false;

    /**
     * Location of the task. Free text for now.
     */
    @Size(max = 100)
    @Column(name = "location", length = 100)
    private String location;

    /**
     * Estimated hours to complete the task.
     * Required by Python's TaskPayload (est_hours: float, must be > 0).
     */
    @Column(name = "est_hours")
    private Double estHours;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public Task() {}

    public Task(String title, String description, String category, SME sme) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.sme = sme;
        this.status = "OPEN";
        this.isPaid = true;
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

    public SME getSme() {
        return sme;
    }

    public void setSme(SME sme) {
        this.sme = sme;
    }

    public Youth getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Youth assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    /**
     * Sets the raw status value.
     *
     * Production code must NOT use this setter: it bypasses the task state
     * machine. Use {@link #assignTo}, {@link #complete}, {@link #verify}, or
     * {@link #sendBackForRevision} instead. It stays public for data loading
     * and tests.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the current status as a {@link TaskStatus}.
     *
     * @throws IllegalArgumentException when the stored status is unknown
     */
    public TaskStatus getStatusEnum() {
        return TaskStatus.from(this.status);
    }

    /**
     * Assigns the task to a youth and starts the work.
     *
     * @throws InvalidTaskTransitionException when the task is not OPEN
     *         (re-assignment and assignment of completed work are rejected)
     */
    public void assignTo(Youth youth) {
        requireStatus(TaskStatus.OPEN, "assign");
        transitionTo(TaskStatus.IN_PROGRESS);
        this.assignedTo = youth;
        this.revisionReason = null;
    }

    /**
     * Marks the task as completed by the assigned youth.
     *
     * @throws InvalidTaskTransitionException when the task is not IN_PROGRESS
     *         (open, already completed, or verified tasks are rejected)
     */
    public void complete() {
        requireStatus(TaskStatus.IN_PROGRESS, "complete");
        transitionTo(TaskStatus.COMPLETED);
    }

    /**
     * Marks the task as verified by the owning SME.
     *
     * @throws InvalidTaskTransitionException when the task is not COMPLETED
     *         (verifying twice is rejected)
     */
    public void verify() {
        requireStatus(TaskStatus.COMPLETED, "verify");
        transitionTo(TaskStatus.VERIFIED);
        this.revisionReason = null;
    }

    /**
     * Sends completed work back to the assigned youth for revision.
     * The task returns to IN_PROGRESS and can be completed and verified again.
     *
     * @param reason mandatory explanation the youth can act on
     * @throws InvalidTaskTransitionException when the task is not COMPLETED
     */
    public void sendBackForRevision(String reason) {
        requireStatus(TaskStatus.COMPLETED, "send back for revision");
        transitionTo(TaskStatus.IN_PROGRESS);
        this.revisionReason = reason;
        this.revisionCount++;
    }

    /**
     * Ensures the task is in the status an operation requires.
     */
    private void requireStatus(TaskStatus expected, String action) {
        TaskStatus current = getStatusEnum();
        if (current != expected) {
            throw new InvalidTaskTransitionException(
                "Cannot " + action + " task " + id
                    + ": expected status " + expected + " but was " + current);
        }
    }

    /**
     * Moves the task to {@code next}, enforcing the task state machine.
     *
     * @throws InvalidTaskTransitionException when the transition is not allowed
     */
    private void transitionTo(TaskStatus next) {
        TaskStatus current = getStatusEnum();
        if (!current.canTransitionTo(next)) {
            throw new InvalidTaskTransitionException(this.id, current, next);
        }
        this.status = next.name();
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public Integer getTier() {
        return tier;
    }

    public void setTier(Integer tier) {
        this.tier = tier;
    }

    public LocalDateTime getReservedUntil() {
        return reservedUntil;
    }

    public void setReservedUntil(LocalDateTime reservedUntil) {
        this.reservedUntil = reservedUntil;
    }

    public boolean isRemote() {
        return remote;
    }

    public void setRemote(boolean remote) {
        this.remote = remote;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getEstHours() {
        return estHours;
    }

    public void setEstHours(Double estHours) {
        this.estHours = estHours;
    }

    public String getRevisionReason() {
        return revisionReason;
    }

    public void setRevisionReason(String revisionReason) {
        this.revisionReason = revisionReason;
    }

    public int getRevisionCount() {
        return revisionCount;
    }

    public void setRevisionCount(int revisionCount) {
        this.revisionCount = revisionCount;
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
        return "Task{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", category='" + category + '\'' +
            ", status='" + status + '\'' +
            ", tier=" + tier +
            ", remote=" + remote +
            ", location='" + location + '\'' +
            ", estHours=" + estHours +
            ", budget=" + budget +
            '}';
    }
}
