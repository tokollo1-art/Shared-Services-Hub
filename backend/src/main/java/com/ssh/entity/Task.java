package com.ssh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
 * @version 1.0
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
    private String status = "OPEN"; // OPEN, IN_PROGRESS, COMPLETED, VERIFIED, CANCELLED

    @Column
    private Double budget;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(name = "is_paid")
    private boolean isPaid = true;

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

    public void setStatus(String status) {
        this.status = status;
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
            ", budget=" + budget +
            '}';
    }
}
