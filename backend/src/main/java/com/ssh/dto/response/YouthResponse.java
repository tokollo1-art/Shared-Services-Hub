package com.ssh.dto.response;

import java.time.LocalDateTime;

/**
 * Youth Response DTO
 *
 * Purpose: Carries youth profile data back to the frontend safely.
 *
 * WHY SSH needs this DTO:
 *   - Flattens the Youth entity so we can include the linked user summary
 *   - Controls exactly which fields the API returns
 *   - Used by YouthController for profile and listing responses
 *   - Includes isAlumni so the frontend can show alumni status
 *
 * TODO: Add verified experience count
 * TODO: Add total income earned
 * TODO: Add skills as a parsed list instead of free text
 *
 * TDD: Write YouthResponseTest first
 *       - shouldMapAllFields_FromEntity()
 *       - shouldIncludeUserSummary()
 *       - shouldIncludeIsAlumniFlag()
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
public class YouthResponse {

    private Long id;

    private Long userId;

    private String userName;

    private String education;

    private String skills;

    private String portfolio;

    private String bio;

    private String location;

    private String availability;

    private boolean isAlumni;

    private LocalDateTime alumniDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public YouthResponse() {}

    public YouthResponse(Long id, String userName, String location, boolean isAlumni) {
        this.id = id;
        this.userName = userName;
        this.location = location;
        this.isAlumni = isAlumni;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(String portfolio) {
        this.portfolio = portfolio;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public boolean isAlumni() {
        return isAlumni;
    }

    public void setAlumni(boolean alumni) {
        isAlumni = alumni;
    }

    public LocalDateTime getAlumniDate() {
        return alumniDate;
    }

    public void setAlumniDate(LocalDateTime alumniDate) {
        this.alumniDate = alumniDate;
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
        return "YouthResponse{" +
            "id=" + id +
            ", userName='" + userName + '\'' +
            ", location='" + location + '\'' +
            ", isAlumni=" + isAlumni +
            '}';
    }
}
