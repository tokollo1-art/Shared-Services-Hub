package com.ssh.dto.agent;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TaskDto
 *
 * Purpose: The shape of a task as the AI service sees it.
 *          Returned by GET /api/v1/tasks/{id}.
 *
 * The evidence agent calls this after TASK_COMPLETED to fetch the task's
 * title, required skills, and tier. Those fields feed the mapper prompt
 * and the confidence scorer.
 *
 * Field names match Python's TaskPayload (camelCase on the wire).
 * requiredSkills is populated from task_skills via a projection query,
 * not from a column on tasks.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
public class TaskDto {

    private Long id;
    private String title;
    private String description;
    private Integer tier;
    private List<String> requiredSkills;
    private Boolean remote;
    private Double estHours;
    private Long smeId;
    private LocalDateTime reservedUntil;

    public TaskDto() {}

    public TaskDto(
        Long id,
        String title,
        String description,
        Integer tier,
        List<String> requiredSkills,
        Boolean remote,
        Double estHours,
        Long smeId,
        LocalDateTime reservedUntil
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.tier = tier;
        this.requiredSkills = requiredSkills;
        this.remote = remote;
        this.estHours = estHours;
        this.smeId = smeId;
        this.reservedUntil = reservedUntil;
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

    public Integer getTier() {
        return tier;
    }

    public void setTier(Integer tier) {
        this.tier = tier;
    }

    public List<String> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<String> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public Boolean getRemote() {
        return remote;
    }

    public void setRemote(Boolean remote) {
        this.remote = remote;
    }

    public Double getEstHours() {
        return estHours;
    }

    public void setEstHours(Double estHours) {
        this.estHours = estHours;
    }

    public Long getSmeId() {
        return smeId;
    }

    public void setSmeId(Long smeId) {
        this.smeId = smeId;
    }

    public LocalDateTime getReservedUntil() {
        return reservedUntil;
    }

    public void setReservedUntil(LocalDateTime reservedUntil) {
        this.reservedUntil = reservedUntil;
    }
}
