package com.ssh.dto.agent;

import java.util.List;

/**
 * YouthStatusDto
 *
 * Purpose: The shape of a youth's availability state.
 *          Returned by GET /api/v1/agent/youth/{id}/status.
 *
 * The matchmaker calls this immediately before every invite to re-check
 * availability. It must be a fresh read — caching is the caller's job.
 *
 * Field names match Python's YouthStatusResponse (camelCase on the wire).
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
public class YouthStatusDto {

    private String availability;
    private Long activeTaskId;
    private List<Long> awaitingVerificationTaskIds;
    private Integer tier;
    private Integer verifiedCount;

    public YouthStatusDto() {}

    public YouthStatusDto(
        String availability,
        Long activeTaskId,
        List<Long> awaitingVerificationTaskIds,
        Integer tier,
        Integer verifiedCount
    ) {
        this.availability = availability;
        this.activeTaskId = activeTaskId;
        this.awaitingVerificationTaskIds = awaitingVerificationTaskIds;
        this.tier = tier;
        this.verifiedCount = verifiedCount;
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public Long getActiveTaskId() {
        return activeTaskId;
    }

    public void setActiveTaskId(Long activeTaskId) {
        this.activeTaskId = activeTaskId;
    }

    public List<Long> getAwaitingVerificationTaskIds() {
        return awaitingVerificationTaskIds;
    }

    public void setAwaitingVerificationTaskIds(List<Long> awaitingVerificationTaskIds) {
        this.awaitingVerificationTaskIds = awaitingVerificationTaskIds;
    }

    public Integer getTier() {
        return tier;
    }

    public void setTier(Integer tier) {
        this.tier = tier;
    }

    public Integer getVerifiedCount() {
        return verifiedCount;
    }

    public void setVerifiedCount(Integer verifiedCount) {
        this.verifiedCount = verifiedCount;
    }
}
