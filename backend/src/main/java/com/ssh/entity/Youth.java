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
 * Slot 2 changes:
 *   - Added ledgerCode (String, unique) — the shareable public ledger URL
 *   - Added ledgerShareEnabled (boolean) — opt-in for the public page
 *
 * @author SSH Team
 * @version 1.1
 * @since 2026
 */
@Entity
@Table(name = "youth")
public class Youth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(name = "is_alumni")
    private boolean isAlumni = false;

    @Column(name = "alumni_date")
    private LocalDateTime alumniDate;

    /**
     * Unguessable, stable code that identifies this youth's public ledger.
     * Generated once on first use. Used in the shareable URL
     * /api/v1/public/ledger/{code}.
     * Contains no PII — it is not derived from the youth's name or ID.
     */
    @Size(max = 32)
    @Column(name = "ledger_code", unique = true, length = 32)
    private String ledgerCode;

    /**
     * Whether the youth has opted in to the public ledger page.
     * When false, /api/v1/public/ledger/{code} returns 404.
     * Defaults to false — sharing is opt-in.
     */
    @Column(name = "ledger_share_enabled")
    private boolean ledgerShareEnabled = false;

    @OneToMany(mappedBy = "youth", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Experience> experiences = new ArrayList<>();

    @OneToMany(mappedBy = "assignedTo")
    private List<Task> tasks = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
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

    public String getLedgerCode() {
        return ledgerCode;
    }

    public void setLedgerCode(String ledgerCode) {
        this.ledgerCode = ledgerCode;
    }

    public boolean isLedgerShareEnabled() {
        return ledgerShareEnabled;
    }

    public void setLedgerShareEnabled(boolean ledgerShareEnabled) {
        this.ledgerShareEnabled = ledgerShareEnabled;
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

    public void addExperience(Experience experience) {
        experiences.add(experience);
        experience.setYouth(this);
    }

    public void removeExperience(Experience experience) {
        experiences.remove(experience);
        experience.setYouth(null);
    }

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
            ", ledgerShareEnabled=" + ledgerShareEnabled +
            '}';
    }
}
