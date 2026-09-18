package com.ssh.dto.response;

import java.time.LocalDateTime;

/**
 * Experience Response DTO
 *
 * Purpose: Carries verified work experience data back to the frontend.
 *
 * WHY SSH needs this DTO:
 *   - Flattens the Experience entity for easy display
 *   - Includes task and SME summaries so the youth can see context
 *   - Used by the Experience Ledger screen
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
public class ExperienceResponse {

    private Long id;

    private String status;

    private boolean isVerified;

    private String feedback;

    private Double rating;

    private LocalDateTime verifiedAt;

    private Long taskId;

    private String taskTitle;

    private String smeBusinessName;

    private Long youthId;

    private String youthName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Getters and setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public String getTaskTitle() { return taskTitle; }
    public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }

    public String getSmeBusinessName() { return smeBusinessName; }
    public void setSmeBusinessName(String smeBusinessName) { this.smeBusinessName = smeBusinessName; }

    public Long getYouthId() { return youthId; }
    public void setYouthId(Long youthId) { this.youthId = youthId; }

    public String getYouthName() { return youthName; }
    public void setYouthName(String youthName) { this.youthName = youthName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
