package com.ssh.entity;

import jakarta.persistence.*;

/**
 * TaskSkill Entity
 *
 * Purpose: Join table linking a task to the skills it requires.
 *          The matchmaker uses this to score youth against a task.
 *
 * Design note: an explicit join entity, not a @ManyToMany.
 * Reason: a task may later mark a skill as "required" vs "nice to have"
 * with a weight column. A ManyToMany has nowhere to put that field.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Entity
@Table(
    name = "task_skills",
    uniqueConstraints = @UniqueConstraint(columnNames = {"task_id", "skill_id"})
)
public class TaskSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    public TaskSkill() {}

    public TaskSkill(Task task, Skill skill) {
        this.task = task;
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

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
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
        return "TaskSkill{" +
            "id=" + id +
            ", taskId=" + (task != null ? task.getId() : null) +
            ", skillId=" + (skill != null ? skill.getId() : null) +
            '}';
    }
}
