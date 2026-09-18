package com.ssh.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * SME Response DTO
 *
 * Purpose: Carries SME profile data back to the frontend safely.
 *
 * WHY SSH needs this DTO:
 *   - Flattens the SME entity so we can include the linked user summary
 *   - Controls exactly which fields the API returns
 *   - Used by SMEController for profile and listing responses
 *   - Includes isVerified so the frontend can show verification status
 *
 * TODO: Add task count summary (open tasks, completed tasks)
 * TODO: Add average rating field
 * TODO: Add logo URL field
 *
 * TDD: Write SMEResponseTest first
 *       - shouldMapAllFields_FromEntity()
 *       - shouldIncludeUserSummary()
 *       - shouldIncludeIsVerifiedFlag()
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Setter
@Getter
public class SMEResponse {

    private Long id;

    private Long userId;

    private String userName;

    private String businessName;

    private String registrationNumber;

    private String industry;

    private String description;

    private Integer employeeCount;

    private String bbbeeLevel;

    private boolean isVerified;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public SMEResponse() {}

    public SMEResponse(Long id, String businessName, String industry, boolean isVerified) {
        this.id = id;
        this.businessName = businessName;
        this.industry = industry;
        this.isVerified = isVerified;
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    // ============================================
    // TO STRING
    // ============================================

    @Override
    public String toString() {
        return "SMEResponse{" +
            "id=" + id +
            ", businessName='" + businessName + '\'' +
            ", industry='" + industry + '\'' +
            ", isVerified=" + isVerified +
            '}';
    }
}
