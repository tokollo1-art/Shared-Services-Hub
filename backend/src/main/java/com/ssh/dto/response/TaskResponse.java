package com.ssh.dto.response;

import java.time.LocalDateTime;

/**
 * Task Response DTO
 *
 * Purpose: Carries task data back to the frontend safely.
 *
 * WHY SSH needs this DTO:
 *   - Flattens the Task entity so we can include SME and Youth summaries
 *   - Controls exactly which fields the API returns
 *   - Used by TaskController for list, detail, and assignment responses
 *   - Includes SME name so the youth can see who posted the task
 *
 * TODO: Add applicant count field
 * TODO: Add average rating of the SME
 * TODO: Add list of required skills
 *
 * TDD: Write TaskResponseTest first
 *       - shouldMapAllFields_FromEntity()
 *       - shouldIncludeSmeSummary()
 *       - shouldIncludeAssignedYouthSummary()
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
public class TaskResponse {

    private Long id;

    private String title;

    private String description;

    private String category;

    private String status;

    private Double budget;

    private Integer durationDays;

    private boolean isPaid;

    private Long smeId;

    private String smeBusinessName;

    private Long assignedToId;

    private String assignedToName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public TaskResponse() {}

    public TaskResponse(Long id, String title, String status) {
        this.id = id;
        this.title = title;
        this.status = status;
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

    public Long getSmeId() {
        return smeId;
    }

    public void setSmeId(Long smeId) {
        this.smeId = smeId;
    }

    public String getSmeBusinessName() {
        return smeBusinessName;
    }

    public void setSmeBusinessName(String smeBusinessName) {
        this.smeBusinessName = smeBusinessName;
    }

    public Long getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
    }

    public String getAssignedToName() {
        return assignedToName;
    }

    public void setAssignedToName(String assignedToName) {
        this.assignedToName = assignedToName;
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
        return "TaskResponse{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", category='" + category + '\'' +
            ", status='" + status + '\'' +
            ", budget=" + budget +
            '}';
    }
}
