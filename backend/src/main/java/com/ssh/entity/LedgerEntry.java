package com.ssh.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * One immutable link in the public verification ledger.
 *
 * Every verified Experience produces exactly one entry. Each entry stores the
 * SHA-256 hash of its canonical payload plus the previous entry's hash, which
 * makes retroactive edits detectable: changing any earlier entry breaks every
 * subsequent hash.
 */
@Entity
@Table(
    name = "ledger_entries",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_ledger_sequence", columnNames = "entry_sequence"),
        @UniqueConstraint(name = "uk_ledger_experience", columnNames = "experience_id")
    }
)
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entry_sequence", nullable = false)
    private Long sequence;

    @Column(name = "experience_id", nullable = false)
    private Long experienceId;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "youth_id", nullable = false)
    private Long youthId;

    private Double rating;

    @Column(length = 1000)
    private String feedback;

    @Column(length = 255)
    private String reference;

    @Column(name = "verified_at", nullable = false)
    private LocalDateTime verifiedAt;

    @Column(name = "previous_hash", nullable = false, length = 64)
    private String previousHash;

    @Column(nullable = false, length = 64)
    private String hash;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public LedgerEntry() {
    }

    public LedgerEntry(Long sequence, Long experienceId, Long taskId, Long youthId,
                       Double rating, String feedback, String reference,
                       LocalDateTime verifiedAt, String previousHash) {
        this.sequence = sequence;
        this.experienceId = experienceId;
        this.taskId = taskId;
        this.youthId = youthId;
        this.rating = rating;
        this.feedback = feedback;
        this.reference = reference;
        this.verifiedAt = verifiedAt;
        this.previousHash = previousHash;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public Long getExperienceId() {
        return experienceId;
    }

    public void setExperienceId(Long experienceId) {
        this.experienceId = experienceId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getYouthId() {
        return youthId;
    }

    public void setYouthId(Long youthId) {
        this.youthId = youthId;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
