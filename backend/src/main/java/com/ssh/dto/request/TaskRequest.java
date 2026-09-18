package com.ssh.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Task Request DTO
 *
 * Purpose: Carries task creation data from the SME to the backend.
 *
 * WHY SSH needs this DTO:
 *   - Only exposes fields the SME should submit
 *   - Validates input before creating a Task
 *   - Prevents clients from setting restricted fields (like id, status, sme)
 *   - Includes optional budget and duration for paid tasks
 *
 * TODO: Add list of required skills for matching
 * TODO: Add preferred location field
 * TODO: Add attachment URL field (portfolio, brief)
 *
 * TDD: Write TaskRequestTest first
 *       - shouldHaveNoViolations_ForValidRequest()
 *       - shouldFailValidation_WhenTitleIsBlank()
 *       - shouldFailValidation_WhenTitleTooLong()
 *       - shouldFailValidation_WhenDescriptionTooLong()
 *       - shouldFailValidation_WhenBudgetIsNegative()
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
public class TaskRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    @Size(max = 1000)
    private String description;

    @NotBlank(message = "Category is required")
    @Size(max = 50)
    private String category;

    @PositiveOrZero(message = "Budget must be zero or positive")
    private Double budget;

    @Positive(message = "Duration must be positive")
    private Integer durationDays;

    private boolean isPaid = true;

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public TaskRequest() {}

    public TaskRequest(String title, String description, String category) {
        this.title = title;
        this.description = description;
        this.category = category;
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

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

    // ============================================
    // TO STRING
    // ============================================

    @Override
    public String toString() {
        return "TaskRequest{" +
            "title='" + title + '\'' +
            ", category='" + category + '\'' +
            ", budget=" + budget +
            ", isPaid=" + isPaid +
            '}';
    }
}
