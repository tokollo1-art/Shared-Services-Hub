package com.ssh.entity;

import jakarta.persistence.*;

/**
 * YouthSkill Entity
 *
 * Purpose: Join table linking a youth to their skills.
 *          Replaces the free-text Youth.skills column once the data
 *          is migrated. The old column stays until then.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Entity
@Table(
    name = "youth_skills",
    uniqueConstraints = @UniqueConstraint(columnNames = {"youth_id", "skill_id"})
)
public class YouthSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "youth_id", nullable = false)
    private Youth youth;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    public YouthSkill() {}

    public YouthSkill(Youth youth, Skill skill) {
        this.youth = youth;
        this.skill = skill;
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

    public Youth getYouth() {
        return youth;
    }

    public void setYouth(Youth youth) {
        this.youth = youth;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    // ============================================
    // TO STRING
    // ============================================

    @Override
    public String toString() {
        return "YouthSkill{" +
            "id=" + id +
            ", youthId=" + (youth != null ? youth.getId() : null) +
            ", skillId=" + (skill != null ? skill.getId() : null) +
            '}';
    }
}
