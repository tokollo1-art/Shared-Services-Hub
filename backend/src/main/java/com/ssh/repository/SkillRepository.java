package com.ssh.repository;

import com.ssh.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * SkillRepository
 *
 * Purpose: Data access for the curated skill taxonomy.
 *
 * The taxonomy is loaded once by the seed loader and read by:
 *   - AgentService: to resolve a youth's skill IDs to skill data
 *   - SkillsController: to expose the taxonomy to the frontend
 *   - The matchmaker: to compare task skills to youth skills
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    Optional<Skill> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Skill> findByCategory(String category);

    List<Skill> findBySlugIn(List<String> slugs);
}
