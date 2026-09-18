package com.ssh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Youth Entity
 *
 * Purpose: Represents a youth program alumni in SSH.
 *          Youth complete tasks, earn income, and build the Experience Ledger.
 *
 * WHY SSH needs this entity:
 *   - Youth are the beneficiaries of the platform
 *   - They complete tasks posted by SMEs
 *   - They build verified experience through the Experience Ledger
 *   - They exit only when permanently placed (become alumni)
 *
 * TODO: Add @JsonIgnore to experiences and tasks fields to prevent circular reference
 * TODO: Add method to calculate total verified experience
 * TODO: Add method to calculate total income earned
 * TODO: Add method to check if youth is ready for alumni status
 *
 * TDD: Write YouthEntityTest first
 *       - shouldHaveNoViolations_ForValidYouth()
 *       - shouldBeActive_ByDefault()
 *       - shouldBecomeAlumni_WhenMarkedAsPlaced()
 *       - shouldAddExperience_ToExperienceList()
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Entity
@Table(name = "youth")
public class Youth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * One-to-One relationship with User.
     * Every Youth has exactly one User account for authentication.
     */
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 255)
    @Column(length = 255)
    private String education;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(columnDefinition = "TEXT")
    private String portfolio;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Size(max = 100)
    @Column(length = 100)
    private String location;

    @Size(max = 50)
    @Column(length = 50)
    private String availability;

    /**
     * Whether the youth has been permanently placed.
     * When true, the youth becomes an SSH Alumni and exits the hub.
     */
    @Column(name = "is_alumni")
    private boolean isAlumni = false;

    /**
     * The date the youth became an alumni (permanently placed).
     * Set only when isAlumni is true.
     */
    @Column(name = "alumni_date")
    private LocalDateTime alumniDate;

    /**
     * One-to-Many relationship with Experience.
     * One youth can have many verified experiences.
     */
    @OneToMany(mappedBy = "youth", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Experience> experiences = new ArrayList<>();

    /**
     * One-to-Many relationship with Task.
     * One youth can be assigned many tasks over time.
     */
    @OneToMany(mappedBy = "assignedTo")
    private List<Task> tasks = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isAlumni = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public Youth() {}

    public Youth(User user) {
        this.user = user;
        this.isAlumni = false;
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

    public List<Experience> getExperiences() {
        return experiences;
    }

    public void setExperiences(List<Experience> experiences) {
        this.experiences = experiences;
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
     * Adds an experience to this youth's experience list.
     */
    public void addExperience(Experience experience) {
        experiences.add(experience);
        experience.setYouth(this);
    }

    /**
     * Removes an experience from this youth's experience list.
     */
    public void removeExperience(Experience experience) {
        experiences.remove(experience);
        experience.setYouth(null);
    }

    /**
     * Marks this youth as an alumni (permanently placed).
     * Called when the youth secures permanent employment.
     */
    public void markAsAlumni() {
        this.isAlumni = true;
        this.alumniDate = LocalDateTime.now();
    }

    // ============================================
    // TO STRING
    // ============================================

    @Override
    public String toString() {
        return "Youth{" +
            "id=" + id +
            ", location='" + location + '\'' +
            ", isAlumni=" + isAlumni +
            ", alumniDate=" + alumniDate +
            '}';
    }
}
