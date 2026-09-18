package com.ssh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.ssh.entity.Task;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SME Entity
 * Purpose: Represents a Small and Medium Enterprise (SME) registered on SSH.
 *          SMEs are the job creators — they post tasks and host youth.
 * WHY SSH needs this entity:
 *   - SMEs are the point of differentiation (free access)
 *   - They verify task completion, which powers the Experience Ledger
 *   - They link to Tasks and Experiences
 * TODO: Add relationship to Corporate sponsors (if applicable)
 * TODO: Add method to calculate active task count
 * TODO: Add method to calculate completed task count
 * TODO: Add @JsonIgnore to tasks field to prevent circular reference
 * TDD: Write SMEEntityTest first
 *       - shouldHaveNoViolations_ForValidSME()
 *       - shouldFailValidation_WhenBusinessNameIsBlank()
 *       - shouldFailValidation_WhenBusinessNameTooLong()
 *       - shouldAddTask_ToTaskList()
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Entity
@Table(name = "smes")
public class SME {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * One-to-One relationship with User.
     * Every SME has exactly one User account for authentication.
     */
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Business name is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String businessName;

    @Size(max = 20)
    @Column(length = 20)
    private String registrationNumber;

    @Size(max = 100)
    @Column(length = 100)
    private String industry;

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    @Column(name = "employee_count")
    private Integer employeeCount;

    @Size(max = 50)
    @Column(length = 50)
    private String bbbeeLevel;

    @Column(name = "is_verified")
    private boolean isVerified = false;

    /**
     * One-to-Many relationship with Task.
     * One SME can post many tasks.
     */
    @OneToMany(mappedBy = "sme", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public SME() {}

    public SME(User user, String businessName) {
        this.user = user;
        this.businessName = businessName;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

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

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
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
     * Adds a task to this SME's task list and sets the bidirectional link.
     */
    public void addTask(Task task) {
        tasks.add(task);
        task.setSme(this);
    }

    /**
     * Removes a task from this SME's task list and clears the bidirectional link.
     */
    public void removeTask(Task task) {
        tasks.remove(task);
        task.setSme(null);
    }

    // ============================================
    // TO STRING
    // ============================================

    @Override
    public String toString() {
        return "SME{" +
            "id=" + id +
            ", businessName='" + businessName + '\'' +
            ", industry='" + industry + '\'' +
            ", isVerified=" + isVerified +
            '}';
    }
}
