package com.ssh.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Youth Profile Request DTO
 *
 * Purpose: Carries youth profile updates from the frontend to the backend.
 *
 * WHY SSH needs this DTO:
 *   - Only exposes fields the youth should edit
 *   - Validates input before updating the Youth entity
 *   - Prevents clients from changing isAlumni or user relationships
 *
 * TODO: Add date of birth field with age validation (must be 18-34)
 * TODO: Add list of preferred job categories
 * TODO: Add availability calendar
 *
 * TDD: Write YouthProfileRequestTest first
 *       - shouldHaveNoViolations_ForValidRequest()
 *       - shouldFailValidation_WhenEducationTooLong()
 *       - shouldFailValidation_WhenBioTooLong()
 *       - shouldFailValidation_WhenLocationTooLong()
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
public class YouthProfileRequest {

    @Size(max = 255)
    private String education;

    private String skills;

    private String portfolio;

    private String bio;

    @Size(max = 100)
    private String location;

    @Size(max = 50)
    private String availability;

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public YouthProfileRequest() {}

    public YouthProfileRequest(String education, String location) {
        this.education = education;
        this.location = location;
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

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

    // ============================================
    // TO STRING
    // ============================================

    @Override
    public String toString() {
        return "YouthProfileRequest{" +
            "education='" + education + '\'' +
            ", location='" + location + '\'' +
            ", availability='" + availability + '\'' +
            '}';
    }
}
