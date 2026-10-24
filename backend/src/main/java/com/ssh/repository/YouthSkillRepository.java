package com.ssh.repository;

import com.ssh.entity.YouthSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * YouthSkillRepository
 *
 * Purpose: Data access for the youth ↔ skill join table.
 *
 * Used by:
 *   - AgentService: to populate each eligible youth's skills list
 *     (the matchmaker's skill-overlap signal)
 *   - The youth profile: to render a youth's skills
 *   - The matching filter: to find youth who have a specific skill
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Repository
public interface YouthSkillRepository extends JpaRepository<YouthSkill, Long> {

    List<YouthSkill> findByYouthId(Long youthId);

    @Query("SELECT ys.skill.slug FROM YouthSkill ys WHERE ys.youth.id = :youthId")
    List<String> findSkillSlugsByYouthId(@Param("youthId") Long youthId);

    @Query("""
        SELECT ys.youth.id, ys.skill.slug
        FROM YouthSkill ys
        WHERE ys.youth.id IN :youthIds
        """)
    List<Object[]> findSkillSlugsByYouthIds(@Param("youthIds") List<Long> youthIds);

    void deleteByYouthId(Long youthId);

    boolean existsByYouthIdAndSkillId(Long youthId, Long skillId);
}
