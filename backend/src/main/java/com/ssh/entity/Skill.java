package com.ssh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Skill Entity
 *
 * Purpose: Represents a skill in the SSH taxonomy.
 *          Skills are loaded from a curated list (ESCO + OFO) and used
 *          by the matchmaker, the evidence agent, and the youth feed.
 *
 * Why this is a table, not a free-text column:
 *   - Filters need structured tags, not comma-separated strings
 *   - The match function needs exact slugs to compare
 *   - The ESCO/OFO mapping is per-skill metadata, not per-youth text
 *
 * The taxonomy is seeded once. Youth and tasks link to it via
 * youth_skills and task_skills join tables.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Entity
@Table(name = "skills")
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Stable, lowercase identifier. Used in APIs, URLs, and seed files.
     * Example: "inventory-coordination"
     */
    @NotBlank(message = "Slug is required")
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    /**
     * Human-readable label. Shown in the UI.
     * Example: "Inventory coordination"
     */
    @NotBlank(message = "Label is required")
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String label;

    /**
     * Category for grouping in filters. Example: "retail", "admin", "tech".
     */
    @Size(max = 50)
    @Column(length = 50)
    private String category;

    /**
     * ESCO concept URI. Optional — the curated subset may not have
     * a mapping for every skill. See the Slot 3 attribution note.
     */
    @Size(max = 500)
    @Column(name = "esco_uri", length = 500)
    private String escoUri;

    /**
     * OFO code (South African occupational framework). Optional.
     * Used for SETA-aligned reporting.
     */
    @Size(max = 20)
    @Column(name = "ofo_code", length = 20)
    private String ofoCode;

    public Skill() {}

    public Skill(String slug, String label, String category) {
        this.slug = slug;
        this.label = label;
        this.category = category;
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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getEscoUri() {
        return escoUri;
    }

    public void setEscoUri(String escoUri) {
        this.escoUri = escoUri;
    }

    public String getOfoCode() {
        return ofoCode;
    }

    public void setOfoCode(String ofoCode) {
        this.ofoCode = ofoCode;
    }

    // ============================================
    // TO STRING
    // ============================================

    @Override
    public String toString() {
        return "Skill{" +
            "id=" + id +
            ", slug='" + slug + '\'' +
            ", label='" + label + '\'' +
            ", category='" + category + '\'' +
            '}';
    }
}
