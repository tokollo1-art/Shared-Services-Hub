package com.ssh.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * SME Profile Request DTO
 *
 * Purpose: Carries SME profile updates from the frontend to the backend.
 *
 * WHY SSH needs this DTO:
 *   - Only exposes fields the SME should edit
 *   - Validates input before updating the SME entity
 *   - Prevents clients from changing isVerified or user relationships
 *
 * TODO: Add optional logo/URL field
 * TODO: Add optional contact information (phone, email, website)
 * TODO: Add optional address fields
 *
 * TDD: Write SMEProfileRequestTest first
 *       - shouldHaveNoViolations_ForValidRequest()
 *       - shouldFailValidation_WhenBusinessNameIsBlank()
 *       - shouldFailValidation_WhenBusinessNameTooLong()
 *       - shouldFailValidation_WhenEmployeeCountIsNegative()
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
public class SMEProfileRequest {

    @NotBlank(message = "Business name is required")
    @Size(max = 100)
    private String businessName;

    @Size(max = 20)
    private String registrationNumber;

    @Size(max = 100)
    private String industry;

    @Size(max = 500)
    private String description;

    @PositiveOrZero(message = "Employee count must be zero or positive")
    private Integer employeeCount;

    @Size(max = 50)
    private String bbbeeLevel;

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public SMEProfileRequest() {}

    public SMEProfileRequest(String businessName) {
        this.businessName = businessName;
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(Integer employeeCount) {
        this.employeeCount = employeeCount;
    }

    public String getBbbeeLevel() {
        return bbbeeLevel;
    }

    public void setBbbeeLevel(String bbbeeLevel) {
        this.bbbeeLevel = bbbeeLevel;
    }

    // ============================================
    // TO STRING
    // ============================================

    @Override
    public String toString() {
        return "SMEProfileRequest{" +
            "businessName='" + businessName + '\'' +
            ", industry='" + industry + '\'' +
            ", bbbeeLevel='" + bbbeeLevel + '\'' +
            '}';
    }
}
