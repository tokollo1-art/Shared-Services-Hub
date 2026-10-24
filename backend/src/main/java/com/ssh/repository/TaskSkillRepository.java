package com.ssh.repository;

import com.ssh.entity.TaskSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * TaskSkillRepository
 *
 * Purpose: Data access for the task ↔ skill join table.
 *
 * Used by:
 *   - AgentService: to fetch a task's required skills for the
 *     eligible-youth response and for the matchmaker's "why" notes
 *   - The SME task detail page: to render required skills
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Repository
public interface TaskSkillRepository extends JpaRepository<TaskSkill, Long> {

    List<TaskSkill> findByTaskId(Long taskId);

    @Query("SELECT ts.skill.slug FROM TaskSkill ts WHERE ts.task.id = :taskId")
    List<String> findSkillSlugsByTaskId(@Param("taskId") Long taskId);

    void deleteByTaskId(Long taskId);

    boolean existsByTaskIdAndSkillId(Long taskId, Long skillId);
}
