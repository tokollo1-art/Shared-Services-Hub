package com.ssh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Experience Entity — The Experience Ledger
 * Purpose: Represents a verified work experience record.
 *          This is the core innovation of SSH: verified work, not self-reported CVs.
 * WHY SSH needs this entity:
 *   - Every completed and verified task is recorded here
 *   - Youth build a portable, verifiable work record
 *   - SMEs verify the work, creating trust
 *   - Funders and corporates can see outcomes, not just activities
 * TODO: Add status enum (PENDING, IN_PROGRESS, COMPLETED, VERIFIED, REJECTED)
 * TODO: Add method to verify experience (SME approval)
 * TODO: Add method to calculate experience value score
 * TODO: Add @JsonIgnore to youth and task to prevent circular reference
 * TDD: Write ExperienceEntityTest first
 *       - shouldHaveNoViolations_ForValidExperience()
 *       - shouldFailValidation_WhenStatusIsBlank()
 *       - shouldSetVerifiedAt_WhenVerified()
 *       - shouldNotVerify_WhenAlreadyVerified()
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Entity
@Table(name = "experiences")
public class Experience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Many-to-One relationship with Youth.
     * Many experiences can belong to one youth.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "youth_id", nullable = false)
    private Youth youth;

    /**
     * Many-to-One relationship with Task.
     * Many experiences can be linked to one task (though typically 1:1).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @NotBlank(message = "Status is required")
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String status = "PENDING"; // PENDING, IN_PROGRESS, COMPLETED, VERIFIED, REJECTED

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Size(max = 1000)
    @Column(length = 1000)
    private String feedback;

    @Column(name = "is_verified")
    private boolean isVerified = false;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column
    private Double rating;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "PENDING";
        }
        if (!this.isVerified) {
            this.isVerified = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public Experience() {}

    public Experience(Youth youth, Task task) {
        this.youth = youth;
        this.task = task;
        this.status = "PENDING";
        this.isVerified = false;
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

    public Youth getYouth() {
        return youth;
    }

    public void setYouth(Youth youth) {
        this.youth = youth;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
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
    // HELPER METHODS
    // ============================================

    /**
     * Marks this experience as verified by the SME.
     * Sets verifiedAt and status.
     */
    public void verify(String feedback, Double rating) {
        this.isVerified = true;
        this.verifiedAt = LocalDateTime.now();
        this.status = "VERIFIED";
        this.feedback = feedback;
        this.rating = rating;
    }

    // ============================================
    // TO STRING
    // ============================================

    @Override
    public String toString() {
        return "Experience{" +
            "id=" + id +
            ", status='" + status + '\'' +
            ", isVerified=" + isVerified +
            ", rating=" + rating +
            '}';
    }
}
