package com.ssh.dto.agent;

import java.util.List;

/**
 * EligibleYouthDto
 *
 * Purpose: The shape of one youth in the matchmaker's candidate list.
 *          Returned by GET /api/v1/agent/tasks/{id}/eligible-youth.
 *
 * Field names and JSON casing match Python's EligibleYouth model
 * (camelCase on the wire, snake_case in Python). Do not rename fields
 * without updating the Python schemas.py contract.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
public class EligibleYouthDto {

    private Long id;
    private List<String> skills;
    private Integer tier;
    private String availability;
    private Integer awaitingVerificationCount;
    private String availabilityWindow;
    private String distanceBucket;

    public EligibleYouthDto() {}

    public EligibleYouthDto(
        Long id,
        List<String> skills,
        Integer tier,
        String availability,
        Integer awaitingVerificationCount,
        String availabilityWindow,
        String distanceBucket
    ) {
        this.id = id;
        this.skills = skills;
        this.tier = tier;
        this.availability = availability;
        this.awaitingVerificationCount = awaitingVerificationCount;
        this.availabilityWindow = availabilityWindow;
        this.distanceBucket = distanceBucket;
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

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public Integer getTier() {
        return tier;
    }

    public void setTier(Integer tier) {
        this.tier = tier;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public Integer getAwaitingVerificationCount() {
        return awaitingVerificationCount;
    }

    public void setAwaitingVerificationCount(Integer awaitingVerificationCount) {
        this.awaitingVerificationCount = awaitingVerificationCount;
    }

    public String getAvailabilityWindow() {
        return availabilityWindow;
    }

    public void setAvailabilityWindow(String availabilityWindow) {
        this.availabilityWindow = availabilityWindow;
    }

    public String getDistanceBucket() {
        return distanceBucket;
    }

    public void setDistanceBucket(String distanceBucket) {
        this.distanceBucket = distanceBucket;
    }
}
